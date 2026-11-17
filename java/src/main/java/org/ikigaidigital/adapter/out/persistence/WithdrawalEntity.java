package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA mapping of the {@code withdrawals} table. The deposit is referenced by its id (a plain foreign-key column)
 * rather than a {@code @ManyToOne}, so no lazy loading or object graph is needed for a read-only list.
 * No @Data/@EqualsAndHashCode/@ToString on purpose: they interfere with Hibernate entity identity and proxies.
 */
@Entity
@Table(name = "withdrawals")
@Getter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class WithdrawalEntity {

    @Id
    private Integer id;

    @Column(name = "time_deposit_id", nullable = false)
    private int timeDepositId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;
}
