package org.ikigaidigital.application.port.in;

import java.util.List;

/** Returns all time deposits, each with its withdrawals. */
public interface GetAllTimeDepositsUseCase {

    List<TimeDepositView> getAllTimeDeposits();
}
