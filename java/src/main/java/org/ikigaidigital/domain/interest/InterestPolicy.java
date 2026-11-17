package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Strategy for the monthly interest of one plan type.
 * Supporting a new plan means adding one implementation; existing code stays closed for modification.
 */
public interface InterestPolicy {

    /** The plan type this policy applies to, e.g. {@code "basic"}. Matched exactly (case-sensitive). */
    String planType();

    /** Unrounded monthly interest for the deposit; {@code 0} when the deposit is not eligible. */
    double monthlyInterest(TimeDeposit deposit);
}
