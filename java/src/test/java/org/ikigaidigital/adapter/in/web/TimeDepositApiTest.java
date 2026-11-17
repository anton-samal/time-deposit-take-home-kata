package org.ikigaidigital.adapter.in.web;

import org.ikigaidigital.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** End-to-end: HTTP → controller → service → calculator → PostgreSQL (Testcontainers), with the real Flyway schema. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestcontainersConfiguration.class)
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration")
class TimeDepositApiTest {

    @Autowired
    private MockMvcTester mvc;
    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insertData() {
        jdbc.execute("TRUNCATE withdrawals, time_deposits");
        jdbc.update("INSERT INTO time_deposits (id, plan_type, days, balance) VALUES "
                + "(1, 'basic', 31, 10000.00), (2, 'premium', 45, 50000.00)");
        jdbc.update("INSERT INTO withdrawals (id, time_deposit_id, amount, date) VALUES "
                + "(1, 1, 500.00, '2026-01-15')");
    }

    @Test
    void getReturnsAllTimeDepositsWithTheirWithdrawals() {
        assertThat(mvc.get().uri("/time-deposits"))
                .hasStatusOk()
                .bodyJson().isStrictlyEqualTo("""
                        [
                          {"id": 1, "planType": "basic", "balance": 10000.00, "days": 31,
                           "withdrawals": [{"id": 1, "amount": 500.00, "date": "2026-01-15"}]},
                          {"id": 2, "planType": "premium", "balance": 50000.00, "days": 45, "withdrawals": []}
                        ]
                        """);
    }

    @Test
    void postAppliesOneMonthOfInterestAndGetShowsTheStoredBalances() {
        assertThat(mvc.post().uri("/time-deposits/balances")).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(mvc.get().uri("/time-deposits"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[*].balance").isEqualTo(List.of(10008.33, 50000.00));
    }
}
