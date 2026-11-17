package org.ikigaidigital.config;

import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.TimeDepositService;
import org.ikigaidigital.application.port.in.UpdateAllBalancesUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.ikigaidigital.application.port.out.WithdrawalRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Wires the framework-free core. The calculator and the service have no Spring annotations,
 * so the domain and application layers don't depend on Spring.
 */
@Configuration(proxyBeanMethods = false)
class TimeDepositConfiguration {

    @Bean
    TimeDepositCalculator timeDepositCalculator() {
        return new TimeDepositCalculator();
    }

    @Bean
    TimeDepositService timeDepositService(TimeDepositRepositoryPort timeDeposits,
                                          WithdrawalRepositoryPort withdrawals,
                                          TimeDepositCalculator calculator) {
        return new TimeDepositService(timeDeposits, withdrawals, calculator);
    }

    /**
     * The balance update as one transaction: lock the rows, apply the interest, save. A second update waits for the
     * lock, so no month of interest is lost. Defined here so the core needs no {@code @Transactional}.
     * {@code @Primary} because the plain service also implements {@link UpdateAllBalancesUseCase}.
     */
    @Bean
    @Primary
    UpdateAllBalancesUseCase transactionalUpdateAllBalances(TimeDepositService service,
                                                            PlatformTransactionManager transactionManager) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return () -> transaction.executeWithoutResult(status -> service.updateAllBalances());
    }
}
