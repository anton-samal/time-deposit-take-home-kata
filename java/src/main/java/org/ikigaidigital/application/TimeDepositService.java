package org.ikigaidigital.application;

import lombok.RequiredArgsConstructor;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.TimeDepositView;
import org.ikigaidigital.application.port.in.UpdateAllBalancesUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.ikigaidigital.application.port.out.WithdrawalRepositoryPort;
import org.ikigaidigital.domain.Withdrawal;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Application service: orchestrates the use cases; the interest rules stay in the domain. */
@RequiredArgsConstructor
public class TimeDepositService implements UpdateAllBalancesUseCase, GetAllTimeDepositsUseCase {

    private final TimeDepositRepositoryPort timeDeposits;
    private final WithdrawalRepositoryPort withdrawals;
    private final TimeDepositCalculator calculator;

    @Override
    public void updateAllBalances() {
        // Must run in one transaction (see TimeDepositConfiguration): the lock is held until the new balances are saved.
        List<TimeDeposit> deposits = timeDeposits.findAllForUpdate();
        calculator.updateBalance(deposits);
        timeDeposits.saveAll(deposits);
    }

    @Override
    public List<TimeDepositView> getAllTimeDeposits() {
        // Two queries in total (deposits + withdrawals) instead of one withdrawal query per deposit.
        Map<Integer, List<Withdrawal>> withdrawalsByDepositId = withdrawals.findAll().stream()
                .collect(Collectors.groupingBy(Withdrawal::timeDepositId));

        return timeDeposits.findAll().stream()
                .map(deposit -> TimeDepositView.of(deposit, withdrawalsByDepositId.getOrDefault(deposit.getId(), List.of())))
                .toList();
    }
}
