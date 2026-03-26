package com.neobank.onboarding.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for user onboarding profile persistence.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {

    /**
     * Find user profile by user ID (reference to auth module).
     */
    Optional<UserProfileEntity> findByUserId(UUID userId);

    /**
     * Find user profile by email.
     */
    Optional<UserProfileEntity> findByEmail(String email);

    /**
     * Check if email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Check if user ID exists.
     */
    boolean existsByUserId(UUID userId);

    /**
     * Update user status.
     */
    @Modifying
    @Query("UPDATE UserProfileEntity p SET p.status = :status, p.updatedAt = CURRENT_TIMESTAMP WHERE p.userId = :userId")
    int updateStatusByUserId(@Param("userId") UUID userId, @Param("status") com.neobank.onboarding.UserStatus status);

    /**
     * Mark user as approved.
     */
    @Modifying
    @Query("UPDATE UserProfileEntity p SET p.status = 'ACTIVE', p.approvedBy = :approverId, " +
           "p.approvedAt = CURRENT_TIMESTAMP, p.updatedAt = CURRENT_TIMESTAMP WHERE p.userId = :userId")
    int approveUser(@Param("userId") UUID userId, @Param("approverId") UUID approverId);
}
