package org.ikigaidigital;

import org.ikigaidigital.domain.interest.BasicInterestPolicy;
import org.ikigaidigital.domain.interest.InterestPolicy;
import org.ikigaidigital.domain.interest.PremiumInterestPolicy;
import org.ikigaidigital.domain.interest.StudentInterestPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Applies one month of interest to each deposit. The rules per plan type are {@link InterestPolicy} strategies;
 * plan types without a policy earn no interest.
 */
public class TimeDepositCalculator {

    private final Map<String, InterestPolicy> policiesByPlanType;

    /** Default plans: basic, student and premium. Kept so existing callers don't break. */
    public TimeDepositCalculator() {
        this(List.of(new BasicInterestPolicy(), new StudentInterestPolicy(), new PremiumInterestPolicy()));
    }

    public TimeDepositCalculator(List<InterestPolicy> policies) {
        // HashMap-backed (not Map.copyOf) so a lookup with a null plan type returns null instead of throwing.
        this.policiesByPlanType = Collections.unmodifiableMap(
                policies.stream().collect(Collectors.toMap(InterestPolicy::planType, Function.identity())));
    }

    public void updateBalance(List<TimeDeposit> xs) {
        for (TimeDeposit deposit : xs) {
            deposit.setBalance(deposit.getBalance() + roundToCents(monthlyInterest(deposit)));
        }
    }

    private double monthlyInterest(TimeDeposit deposit) {
        InterestPolicy policy = policiesByPlanType.get(deposit.getPlanType());
        return policy == null ? 0 : policy.monthlyInterest(deposit);
    }

    private static double roundToCents(double amount) {
        // new BigDecimal(double) on purpose: it keeps the exact binary value, as the original code did.
        // BigDecimal.valueOf(double) would round some half-cent amounts differently.
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
