package org.ikigaidigital.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.ikigaidigital.application.port.out.WithdrawalRepositoryPort;
import org.ikigaidigital.domain.Withdrawal;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Outbound adapter: maps the {@code withdrawals} table to {@link Withdrawal}. */
@Component
@RequiredArgsConstructor
class WithdrawalPersistenceAdapter implements WithdrawalRepositoryPort {

    private final WithdrawalJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Withdrawal> findAll() {
        return repository.findAll(Sort.by("date", "id")).stream()
                .map(entity -> new Withdrawal(entity.getId(), entity.getTimeDepositId(), entity.getAmount(), entity.getDate()))
                .toList();
    }
}
