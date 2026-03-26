package com.neobank.fraud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for fraud blacklist persistence.
 */
@Repository
public interface BlacklistRepository extends JpaRepository<BlacklistEntity, UUID> {

    /**
     * Find blacklist entry by entity type and value.
     */
    Optional<BlacklistEntity> findByEntityTypeAndEntityValue(
            BlacklistEntity.BlacklistEntityType entityType,
            String entityValue
    );

    /**
     * Check if an IP address is blacklisted.
     */
    @Query("SELECT COUNT(b) > 0 FROM BlacklistEntity b WHERE b.entityType = 'IP_ADDRESS' " +
           "AND b.entityValue = :ipAddress AND b.active = true")
    boolean isIpAddressBlacklisted(@Param("ipAddress") String ipAddress);

    /**
     * Check if an account ID is blacklisted.
     */
    @Query("SELECT COUNT(b) > 0 FROM BlacklistEntity b WHERE b.entityType = 'ACCOUNT_ID' " +
           "AND b.entityValue = :accountId AND b.active = true")
    boolean isAccountIdBlacklisted(@Param("accountId") String accountId);

    /**
     * Check if a user ID is blacklisted.
     */
    @Query("SELECT COUNT(b) > 0 FROM BlacklistEntity b WHERE b.entityType = 'USER_ID' " +
           "AND b.entityValue = :userId AND b.active = true")
    boolean isUserIdBlacklisted(@Param("userId") String userId);

    /**
     * Find all active blacklist entries by entity type.
     */
    List<BlacklistEntity> findByEntityTypeAndActiveTrue(BlacklistEntity.BlacklistEntityType entityType);

    /**
     * Find all active blacklist entries.
     */
    List<BlacklistEntity> findByActiveTrueOrderByCreatedAtDesc();

    /**
     * Find all high severity blacklist entries.
     */
    List<BlacklistEntity> findBySeverityAndActiveTrue(BlacklistEntity.BlacklistSeverity severity);

    /**
     * Count active blacklist entries.
     */
    long countByActiveTrue();

    /**
     * Count active blacklist entries by entity type.
     */
    long countByEntityTypeAndActiveTrue(BlacklistEntity.BlacklistEntityType entityType);
}
