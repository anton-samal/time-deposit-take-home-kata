package org.ikigaidigital.config;

import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.TimeDepositService;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.ikigaidigital.application.port.out.WithdrawalRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
