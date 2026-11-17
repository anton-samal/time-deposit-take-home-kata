package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.TimeDepositView;
import org.ikigaidigital.domain.Withdrawal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TimeDepositServiceTest {

    private static final double TOLERANCE = 1e-9;

    private final InMemoryTimeDepositRepository timeDeposits = new InMemoryTimeDepositRepository(
            new TimeDeposit(1, "basic", 10000.00, 31),
            new TimeDeposit(2, "premium", 10000.00, 46),
            new TimeDeposit(3, "student", 10000.00, 10));

    private final Withdrawal firstWithdrawal = new Withdrawal(1, 1, new BigDecimal("100.00"), LocalDate.of(2026, 1, 15));
    private final Withdrawal secondWithdrawal = new Withdrawal(2, 1, new BigDecimal("50.00"), LocalDate.of(2026, 2, 15));
    private final Withdrawal premiumWithdrawal = new Withdrawal(3, 2, new BigDecimal("25.50"), LocalDate.of(2026, 3, 1));

    private final TimeDepositService service = new TimeDepositService(
            timeDeposits,
            () -> List.of(firstWithdrawal, secondWithdrawal, premiumWithdrawal),
            new TimeDepositCalculator());

    @Test
    void updateAllBalancesStoresOneMonthOfInterestForEveryDeposit() {
        service.updateAllBalances();

        assertThat(timeDeposits.get(1).getBalance()).isCloseTo(10008.33, within(TOLERANCE));
        assertThat(timeDeposits.get(2).getBalance()).isCloseTo(10041.67, within(TOLERANCE));
        assertThat(timeDeposits.get(3).getBalance()).isEqualTo(10000.00);
    }

    @Test
    void getAllTimeDepositsReturnsEachDepositWithItsOwnWithdrawals() {
        List<TimeDepositView> result = service.getAllTimeDeposits();

        assertThat(result).containsExactly(
                new TimeDepositView(1, "basic", 10000.00, 31, List.of(firstWithdrawal, secondWithdrawal)),
                new TimeDepositView(2, "premium", 10000.00, 46, List.of(premiumWithdrawal)),
                new TimeDepositView(3, "student", 10000.00, 10, List.of()));
    }
}
