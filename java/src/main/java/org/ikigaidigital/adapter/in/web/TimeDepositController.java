package org.ikigaidigital.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.ikigaidigital.adapter.in.web.api.TimeDepositsApi;
import org.ikigaidigital.adapter.in.web.model.TimeDepositResponse;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.UpdateAllBalancesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Inbound adapter: implements the interface generated from the OpenAPI contract and delegates to the use cases. */
@RestController
@RequiredArgsConstructor
class TimeDepositController implements TimeDepositsApi {

    private final GetAllTimeDepositsUseCase getAllTimeDeposits;
    private final UpdateAllBalancesUseCase updateAllBalances;

    @Override
    public ResponseEntity<List<TimeDepositResponse>> getAllTimeDeposits() {
        return ResponseEntity.ok(getAllTimeDeposits.getAllTimeDeposits().stream()
                .map(TimeDepositResponseMapper::toResponse)
                .toList());
    }

    @Override
    public ResponseEntity<Void> updateAllBalances() {
        updateAllBalances.updateAllBalances();
        return ResponseEntity.noContent().build();
    }
}
