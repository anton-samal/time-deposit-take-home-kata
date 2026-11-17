package org.ikigaidigital.domain.interest;

/** Student plan: 3% a year, paid monthly after the first 30 days, and only during the first year. */
public final class StudentInterestPolicy extends AnnualRateInterestPolicy {

    /** "No interest after 1 year", with one year = 365 days: day 365 still earns interest, day 366 doesn't. */
    private static final int LAST_INTEREST_DAY = 365;

    public StudentInterestPolicy() {
        super("student", 0.03);
    }

    @Override
    protected boolean isEligible(int days) {
        return days > NO_INTEREST_PERIOD_DAYS && days <= LAST_INTEREST_DAY;
    }
}
