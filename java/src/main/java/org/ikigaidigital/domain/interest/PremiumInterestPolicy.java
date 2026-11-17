package org.ikigaidigital.domain.interest;

/** Premium plan: 5% a year, paid monthly; interest starts after 45 days. */
public final class PremiumInterestPolicy extends AnnualRateInterestPolicy {

    private static final int INTEREST_START_AFTER_DAYS = 45;

    public PremiumInterestPolicy() {
        super("premium", 0.05);
    }

    @Override
    protected boolean isEligible(int days) {
        // The general 30-day rule is kept explicit, even though the 45-day rule already implies it.
        return days > NO_INTEREST_PERIOD_DAYS && days > INTEREST_START_AFTER_DAYS;
    }
}
