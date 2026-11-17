package org.ikigaidigital.application.port.out;

import org.ikigaidigital.domain.Withdrawal;

import java.util.List;

/** Read access to withdrawals, implemented by an outbound adapter. */
public interface WithdrawalRepositoryPort {

    /** All withdrawals, ordered by date and id. */
    List<Withdrawal> findAll();
}
