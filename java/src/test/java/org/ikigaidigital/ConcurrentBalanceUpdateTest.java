package org.ikigaidigital;

import org.ikigaidigital.application.port.in.UpdateAllBalancesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Overlapping balance updates must not lose interest: N parallel updates must give exactly N months of interest.
 * Runs against real PostgreSQL, because the protection is a database row lock.
 */
@SpringBootTest
@Import(PostgresTestcontainersConfiguration.class)
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration")
class ConcurrentBalanceUpdateTest {

    private static final int PARALLEL_UPDATES = 8;

    @Autowired
    private UpdateAllBalancesUseCase updateAllBalances;
    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insertDeposit() {
        jdbc.execute("TRUNCATE withdrawals, time_deposits");
        jdbc.update("INSERT INTO time_deposits (id, plan_type, days, balance) VALUES (1, 'basic', 31, 10000.00)");
    }

    @Test
    void parallelUpdatesApplyOneMonthOfInterestEach() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_UPDATES)) {
            List<Future<?>> updates = IntStream.range(0, PARALLEL_UPDATES)
                    .<Future<?>>mapToObj(i -> executor.submit(() -> {
                        start.await();
                        updateAllBalances.updateAllBalances();
                        return null;
                    }))
                    .toList();

            start.countDown();
            for (Future<?> update : updates) {
                update.get();
            }
        }

        assertThat(storedBalance()).isEqualByComparingTo(balanceAfterMonths(PARALLEL_UPDATES));
    }

    private BigDecimal storedBalance() {
        return jdbc.queryForObject("SELECT balance FROM time_deposits WHERE id = 1", BigDecimal.class);
    }

    /** Expected result from the calculator itself, applied sequentially, rounded to cents like the database column. */
    private static BigDecimal balanceAfterMonths(int months) {
        TimeDeposit deposit = new TimeDeposit(1, "basic", 10000.00, 31);
        TimeDepositCalculator calculator = new TimeDepositCalculator();
        for (int month = 0; month < months; month++) {
            calculator.updateBalance(List.of(deposit));
            deposit.setBalance(toCents(deposit.getBalance()).doubleValue());
        }
        return toCents(deposit.getBalance());
    }

    private static BigDecimal toCents(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }
}
