package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface TimeDepositJpaRepository extends JpaRepository<TimeDepositEntity, Integer> {

    /** {@code SELECT … FOR UPDATE}: other transactions that want these rows wait until this one ends. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TimeDepositEntity t order by t.id")
    List<TimeDepositEntity> findAllForUpdate();
}
