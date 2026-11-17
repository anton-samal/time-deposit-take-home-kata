package org.ikigaidigital.adapter.in.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.ikigaidigital.adapter.in.web.model.TimeDepositResponse;
import org.ikigaidigital.adapter.in.web.model.WithdrawalResponse;
import org.ikigaidigital.application.port.in.TimeDepositView;
import org.ikigaidigital.domain.Withdrawal;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Maps the application read model to the generated API models, so generated classes never reach the core. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class TimeDepositResponseMapper {

    private static final int MONEY_SCALE = 2;

    static TimeDepositResponse toResponse(TimeDepositView view) {
        return new TimeDepositResponse(
                view.id(),
                view.planType(),
                toMoney(view.balance()),
                view.days(),
                view.withdrawals().stream().map(TimeDepositResponseMapper::toResponse).toList());
    }

    private static WithdrawalResponse toResponse(Withdrawal withdrawal) {
        return new WithdrawalResponse(withdrawal.id(), withdrawal.amount(), withdrawal.date());
    }

    /** The domain balance is a double; the API returns it with 2 decimals (e.g. 10008.33, not 10008.330000000002). */
    private static BigDecimal toMoney(Double balance) {
        return BigDecimal.valueOf(balance).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
