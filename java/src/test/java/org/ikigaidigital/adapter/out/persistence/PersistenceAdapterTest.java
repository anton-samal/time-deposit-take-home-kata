package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.PostgresTestcontainersConfiguration;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.domain.Withdrawal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/** Runs the adapters against a real PostgreSQL with the real Flyway schema (each test is rolled back). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresTestcontainersConfiguration.class, TimeDepositPersistenceAdapter.class, WithdrawalPersistenceAdapter.class})
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration")
class PersistenceAdapterTest {

    @Autowired
    private TimeDepositJpaRepository timeDepositJpaRepository;
    @Autowired
    private WithdrawalJpaRepository withdrawalJpaRepository;
    @Autowired
    private TimeDepositPersistenceAdapter timeDeposits;
    @Autowired
    private WithdrawalPersistenceAdapter withdrawals;

    @BeforeEach
    void insertData() {
        timeDepositJpaRepository.saveAll(List.of(
                new TimeDepositEntity(2, "premium", 46, new BigDecimal("50000.00")),
                new TimeDepositEntity(1, "basic", 31, new BigDecimal("10000.50"))));
        withdrawalJpaRepository.saveAll(List.of(
                new WithdrawalEntity(2, 1, new BigDecimal("250.00"), LocalDate.of(2026, 2, 15)),
                new WithdrawalEntity(1, 1, new BigDecimal("500.00"), LocalDate.of(2026, 1, 15))));
        timeDepositJpaRepository.flush();
        withdrawalJpaRepository.flush();
    }

    @Test
    void findAllReturnsDepositsOrderedByIdWithDecimalBalanceAsDouble() {
        List<TimeDeposit> result = timeDeposits.findAll();

        assertThat(result).extracting(TimeDeposit::getId, TimeDeposit::getPlanType, TimeDeposit::getBalance, TimeDeposit::getDays)
                .containsExactly(
                        tuple(1, "basic", 10000.50, 31),
                        tuple(2, "premium", 50000.00, 46));
    }

    @Test
    void saveAllStoresNewBalancesRoundedToCents() {
        // 10000.00 + 8.33 in double arithmetic is 10008.330000000002; the column keeps 2 decimals.
        double balanceAfterInterest = 10000.00 + 8.33;
        timeDeposits.saveAll(List.of(new TimeDeposit(1, "basic", balanceAfterInterest, 31)));
        timeDepositJpaRepository.flush();

        assertThat(timeDepositJpaRepository.findById(1).orElseThrow().getBalance()).isEqualByComparingTo("10008.33");
        assertThat(timeDepositJpaRepository.findById(2).orElseThrow().getBalance()).isEqualByComparingTo("50000.00");
    }

    @Test
    void findAllWithdrawalsReturnsThemOrderedByDate() {
        assertThat(withdrawals.findAll()).containsExactly(
                new Withdrawal(1, 1, new BigDecimal("500.00"), LocalDate.of(2026, 1, 15)),
                new Withdrawal(2, 1, new BigDecimal("250.00"), LocalDate.of(2026, 2, 15)));
    }
}
