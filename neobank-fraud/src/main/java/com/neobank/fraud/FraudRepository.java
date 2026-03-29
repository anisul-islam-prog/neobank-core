package com.neobank.fraud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for fraud alert persistence.
 */
@Repository
public interface FraudRepository extends JpaRepository<FraudEntity, UUID> {

    /**
     * Find fraud alerts by transfer ID.
     */
    Optional<FraudEntity> findByTransferId(UUID transferId);

    /**
     * Find all fraud alerts by from account ID.
     */
    List<FraudEntity> findByFromAccountIdOrderByCreatedAtDesc(UUID fromAccountId);

    /**
     * Find all fraud alerts by to account ID.
     */
    List<FraudEntity> findByToAccountIdOrderByCreatedAtDesc(UUID toAccountId);

    /**
     * Find all pending fraud alerts ordered by creation date.
     */
    List<FraudEntity> findByStatusOrderByCreatedAtDesc(FraudEntity.FraudStatus status);

    /**
     * Find fraud alerts created after a specific timestamp.
     */
    @Query("SELECT f FROM FraudEntity f WHERE f.createdAt >= :timestamp ORDER BY f.createdAt DESC")
    List<FraudEntity> findByCreatedAtAfter(@Param("timestamp") Instant timestamp);

    /**
     * Count fraud alerts by status.
     */
    long countByStatus(FraudEntity.FraudStatus status);

    /**
     * Count fraud alerts by from account ID and status.
     */
    long countByFromAccountIdAndStatus(UUID fromAccountId, FraudEntity.FraudStatus status);

    /**
     * Update fraud alert status.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FraudEntity f SET f.status = :status, f.reviewedAt = CURRENT_TIMESTAMP, " +
           "f.reviewedBy = :reviewedById, f.reviewNotes = :notes WHERE f.id = :alertId")
    int updateStatus(
            @Param("alertId") UUID alertId,
            @Param("status") FraudEntity.FraudStatus status,
            @Param("reviewedById") UUID reviewedById,
            @Param("notes") String notes
    );

    /**
     * Mark fraud alert as confirmed fraud.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FraudEntity f SET f.status = 'CONFIRMED_FRAUD', f.reviewedAt = CURRENT_TIMESTAMP, " +
           "f.reviewedBy = :reviewedById WHERE f.id = :alertId")
    int confirmFraud(@Param("alertId") UUID alertId, @Param("reviewedById") UUID reviewedById);

    /**
     * Mark fraud alert as false positive.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FraudEntity f SET f.status = 'FALSE_POSITIVE', f.reviewedAt = CURRENT_TIMESTAMP, " +
           "f.reviewedBy = :reviewedById WHERE f.id = :alertId")
    int markAsFalsePositive(@Param("alertId") UUID alertId, @Param("reviewedById") UUID reviewedById);
}
