package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * In-memory fake of the out-port. It stores copies, like a real database, so a balance change
 * only becomes visible after {@link #saveAll}.
 */
class InMemoryTimeDepositRepository implements TimeDepositRepositoryPort {

    private final Map<Integer, TimeDeposit> store = new TreeMap<>();

    InMemoryTimeDepositRepository(TimeDeposit... deposits) {
        saveAll(List.of(deposits));
    }

    @Override
    public List<TimeDeposit> findAll() {
        // TreeMap keeps the deposits ordered by id, as the port requires.
        return store.values().stream()
                .map(InMemoryTimeDepositRepository::copy)
                .toList();
    }

    /** No concurrency in unit tests; locking is covered by ConcurrentBalanceUpdateTest against PostgreSQL. */
    @Override
    public List<TimeDeposit> findAllForUpdate() {
        return findAll();
    }

    @Override
    public void saveAll(List<TimeDeposit> deposits) {
        deposits.forEach(deposit -> store.put(deposit.getId(), copy(deposit)));
    }

    TimeDeposit get(int id) {
        return copy(store.get(id));
    }

    private static TimeDeposit copy(TimeDeposit deposit) {
        return new TimeDeposit(deposit.getId(), deposit.getPlanType(), deposit.getBalance(), deposit.getDays());
    }
}
