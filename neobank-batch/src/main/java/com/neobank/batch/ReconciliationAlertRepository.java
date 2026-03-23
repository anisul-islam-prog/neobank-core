package com.neobank.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for reconciliation alerts.
 */
public interface ReconciliationAlertRepository extends JpaRepository<ReconciliationAlert, UUID> {

    List<ReconciliationAlert> findByStatusOrderByAlertDateDesc(ReconciliationAlert.AlertStatus status);

    @Query("SELECT r FROM ReconciliationAlert r WHERE r.alertDate >= :date ORDER BY r.alertDate DESC")
    List<ReconciliationAlert> findByDate(Instant date);
}
