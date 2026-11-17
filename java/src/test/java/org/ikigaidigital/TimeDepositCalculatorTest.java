package org.ikigaidigital;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Characterization tests for {@link TimeDepositCalculator#updateBalance}. The README states the current behaviour is correct,
 * so these tests pin it down before any refactoring.
 */
class TimeDepositCalculatorTest {

    /** Balances are doubles; compare with a tolerance far below one cent. */
    private static final double TOLERANCE = 1e-9;

    private final TimeDepositCalculator calculator = new TimeDepositCalculator();

    @ParameterizedTest(name = "{0} plan on day {1}: 10000.00 -> {2}")
    @CsvSource({
            // No interest for the first 30 days, for every plan
            "basic,    29, 10000.00",
            "basic,    30, 10000.00",
            "basic,    31, 10008.33",  // 10000 * 1% / 12 = 8.333 -> rounded down to 8.33
            "student,  29, 10000.00",
            "student,  30, 10000.00",
            "student,  31, 10025.00",  // 10000 * 3% / 12 = 25.00
            "premium,  29, 10000.00",
            "premium,  30, 10000.00",
            "premium,  31, 10000.00",  // premium has its own, later start
            // Premium: interest starts after 45 days
            "premium,  44, 10000.00",
            "premium,  45, 10000.00",
            "premium,  46, 10041.67",  // 10000 * 5% / 12 = 41.666 -> rounded up to 41.67
            // Student: no interest after one year (365 days)
            "student, 364, 10025.00",
            "student, 365, 10025.00",
            "student, 366, 10000.00",
    })
    void appliesMonthlyInterestDependingOnPlanAndDays(String planType, int days, double expectedBalance) {
        TimeDeposit deposit = new TimeDeposit(1, planType, 10000.00, days);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isCloseTo(expectedBalance, within(TOLERANCE));
    }

    @Test
    void roundsInterestToCentsBeforeAddingItToTheBalance() {
        // 1234567.00 * 1% / 12 = 1028.80583... -> 1028.81
        TimeDeposit deposit = new TimeDeposit(1, "basic", 1234567.00, 45);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isCloseTo(1235595.81, within(TOLERANCE));
    }

    @ParameterizedTest(name = "plan type \"{0}\" earns no interest")
    @ValueSource(strings = {"gold", "Basic"})
    void earnsNoInterestWhenPlanTypeDoesNotMatchExactly(String planType) {
        TimeDeposit deposit = new TimeDeposit(1, planType, 10000.00, 100);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isEqualTo(10000.00);
    }

    @Test
    void updatesEachDepositInTheListIndependently() {
        TimeDeposit basic = new TimeDeposit(1, "basic", 10000.00, 31);
        TimeDeposit student = new TimeDeposit(2, "student", 20000.00, 31);
        TimeDeposit premium = new TimeDeposit(3, "premium", 10000.00, 46);

        calculator.updateBalance(List.of(basic, student, premium));

        assertThat(basic.getBalance()).isCloseTo(10008.33, within(TOLERANCE));
        assertThat(student.getBalance()).isCloseTo(20050.00, within(TOLERANCE));
        assertThat(premium.getBalance()).isCloseTo(10041.67, within(TOLERANCE));
    }

    @Test
    void appliesOneMonthOfInterestPerCallWithoutChangingDays() {
        TimeDeposit deposit = new TimeDeposit(1, "student", 10000.00, 60);

        calculator.updateBalance(List.of(deposit));
        calculator.updateBalance(List.of(deposit));

        // 10000.00 + 25.00 = 10025.00, then 10025.00 * 3% / 12 = 25.0625 -> 25.06
        assertThat(deposit.getBalance()).isCloseTo(10050.06, within(TOLERANCE));
        assertThat(deposit.getDays()).isEqualTo(60);
    }
}
