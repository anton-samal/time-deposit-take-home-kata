package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA mapping of the {@code time_deposits} table. Never leaves the persistence adapter.
 * No @Data/@EqualsAndHashCode/@ToString on purpose: they interfere with Hibernate entity identity and proxies.
 */
@Entity
@Table(name = "time_deposits")
@Getter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class TimeDepositEntity {

    @Id
    private Integer id;

    @Column(name = "plan_type", nullable = false)
    private String planType;

    @Column(nullable = false)
    private int days;

    /** The only field that changes after creation. */
    @Setter(AccessLevel.PACKAGE)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
}
