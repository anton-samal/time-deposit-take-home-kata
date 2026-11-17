package org.ikigaidigital.application.port.in;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.domain.Withdrawal;

import java.util.List;

/**
 * Read model for "get all time deposits": the deposit's fields plus its withdrawals.
 * Needed because {@link TimeDeposit} has no withdrawals and must not change.
 */
public record TimeDepositView(int id, String planType, Double balance, int days, List<Withdrawal> withdrawals) {

    public TimeDepositView {
        withdrawals = List.copyOf(withdrawals);
    }

    public static TimeDepositView of(TimeDeposit deposit, List<Withdrawal> withdrawals) {
        return new TimeDepositView(deposit.getId(), deposit.getPlanType(), deposit.getBalance(), deposit.getDays(), withdrawals);
    }
}
