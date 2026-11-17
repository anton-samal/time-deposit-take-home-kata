package org.ikigaidigital.domain.interest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.ikigaidigital.TimeDeposit;

/**
 * Template for plans that pay a fixed annual rate monthly: {@code balance * annualRate / 12}.
 * Subclasses only decide on which days a deposit is eligible.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AnnualRateInterestPolicy implements InterestPolicy {

    /** No interest is applied for the first 30 days for any existing plan. */
    protected static final int NO_INTEREST_PERIOD_DAYS = 30;

    private static final int MONTHS_PER_YEAR = 12;

    private final String planType;
    private final double annualRate;

    @Override
    public final String planType() {
        return planType;
    }

    @Override
    public final double monthlyInterest(TimeDeposit deposit) {
        // Same operation order as the original code (balance * rate / 12), so double results are identical.
        return isEligible(deposit.getDays()) ? deposit.getBalance() * annualRate / MONTHS_PER_YEAR : 0;
    }

    protected abstract boolean isEligible(int days);
}
