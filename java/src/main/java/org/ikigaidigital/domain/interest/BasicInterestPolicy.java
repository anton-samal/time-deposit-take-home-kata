package org.ikigaidigital.domain.interest;

/** Basic plan: 1% a year, paid monthly after the first 30 days. */
public final class BasicInterestPolicy extends AnnualRateInterestPolicy {

    public BasicInterestPolicy() {
        super("basic", 0.01);
    }

    @Override
    protected boolean isEligible(int days) {
        return days > NO_INTEREST_PERIOD_DAYS;
    }
}
