package org.ikigaidigital.application.port.out;

import org.ikigaidigital.TimeDeposit;

import java.util.List;

/** Storage of time deposits, implemented by an outbound adapter. */
public interface TimeDepositRepositoryPort {

    /** All time deposits, ordered by id. */
    List<TimeDeposit> findAll();

    /** Stores the current balances of the given deposits, all or nothing. */
    void saveAll(List<TimeDeposit> deposits);
}
