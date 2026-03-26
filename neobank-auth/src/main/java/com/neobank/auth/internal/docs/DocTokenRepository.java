package com.neobank.auth.internal.docs;

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
 * Repository for documentation access tokens.
 */
@Repository
public interface DocTokenRepository extends JpaRepository<DocTokenEntity, UUID> {

    /**
     * Find token by token string.
     */
    Optional<DocTokenEntity> findByToken(String token);

    /**
     * Find all active tokens for a user.
     */
    List<DocTokenEntity> findByCreatedByAndActiveTrue(UUID createdBy);

    /**
     * Find all active tokens (for admin listing).
     */
    List<DocTokenEntity> findByActiveTrueOrderByCreatedAtDesc();

    /**
     * Check if token exists.
     */
    boolean existsByToken(String token);

    /**
     * Deactivate all tokens for a user.
     */
    @Modifying
    @Query("UPDATE DocTokenEntity t SET t.active = false WHERE t.createdBy = :userId AND t.active = true")
    int deactivateAllForUser(@Param("userId") UUID userId);

    /**
     * Expire old tokens (cleanup).
     */
    @Modifying
    @Query("UPDATE DocTokenEntity t SET t.active = false WHERE t.expiresAt < :now AND t.active = true")
    int expireOldTokens(@Param("now") Instant now);
}
