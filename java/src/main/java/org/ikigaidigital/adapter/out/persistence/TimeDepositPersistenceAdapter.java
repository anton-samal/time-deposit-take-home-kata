package org.ikigaidigital.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Outbound adapter: maps between the {@code time_deposits} table and {@link TimeDeposit}.
 * The database stores the balance as DECIMAL(19,2); the domain keeps {@code Double}, because {@link TimeDeposit} must not change.
 */
@Component
@RequiredArgsConstructor
class TimeDepositPersistenceAdapter implements TimeDepositRepositoryPort {

    private static final int MONEY_SCALE = 2;

    private final TimeDepositJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<TimeDeposit> findAll() {
        return repository.findAll(Sort.by("id")).stream()
                .map(entity -> new TimeDeposit(entity.getId(), entity.getPlanType(), entity.getBalance().doubleValue(), entity.getDays()))
                .toList();
    }

    @Override
    @Transactional
    public void saveAll(List<TimeDeposit> deposits) {
        Map<Integer, TimeDeposit> depositsById = deposits.stream()
                .collect(Collectors.toMap(TimeDeposit::getId, Function.identity()));

        // Only the balance changes; the managed entities are flushed on commit.
        repository.findAllById(depositsById.keySet())
                .forEach(entity -> entity.setBalance(toMoney(depositsById.get(entity.getId()).getBalance())));
    }

    /** {@code BigDecimal.valueOf} uses the shortest decimal form of the double (e.g. 10008.33, not 10008.3299999...). */
    private static BigDecimal toMoney(Double balance) {
        return BigDecimal.valueOf(balance).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
