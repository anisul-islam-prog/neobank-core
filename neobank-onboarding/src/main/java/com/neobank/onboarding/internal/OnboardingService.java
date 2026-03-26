package com.neobank.onboarding.internal;

import com.neobank.auth.UserRole;
import com.neobank.onboarding.ApprovalResult;
import com.neobank.onboarding.UserStatus;
import com.neobank.onboarding.api.UserAccountRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Internal onboarding service implementing user registration and approval workflow.
 * Handles business-side: UserStatus, KYC, Approval workflows.
 * Uses schema_onboarding for data isolation.
 */
@Service
@Transactional
public class OnboardingService {

    private static final Logger log = LoggerFactory.getLogger(OnboardingService.class);

    private final UserProfileRepository userProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OnboardingService(UserProfileRepository userProfileRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.userProfileRepository = userProfileRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Register a new user (public registration).
     * Creates profile with PENDING status and publishes event for auth module.
     *
     * @param username the username
     * @param email the email
     * @param password the password (will be hashed by auth module)
     * @return the user ID
     */
    public OnboardingResult registerUser(String username, String email, String password) {
        // Check if email already exists
        if (userProfileRepository.existsByEmail(email)) {
            return OnboardingResult.failure("Email already registered: " + email);
        }

        UUID userId = UUID.randomUUID();

        // Create user profile (business side)
        UserProfileEntity profile = new UserProfileEntity();
        profile.setId(UUID.randomUUID());
        profile.setUserId(userId);
        profile.setEmail(email);
        profile.setStatus(UserStatus.PENDING);
        profile.setMustChangePassword(false);
        profile.setKycVerified(false);
        profile.setCreatedAt(Instant.now());

        userProfileRepository.save(profile);

        // Publish event for auth module to create credentials
        eventPublisher.publishEvent(UserAccountRequestedEvent.of(userId, username, email));

        log.info("User registered (PENDING approval): {} ({})", username, userId);

        return OnboardingResult.success(userId, "Registration successful. Awaiting approval.");
    }

    /**
     * Approve a pending user (promote to ACTIVE).
     * Only MANAGER, RELATIONSHIP_OFFICER, or SYSTEM_ADMIN can approve.
     */
    public ApprovalResult approveUser(UUID userId, UUID approverId, UserRole approverRole) {
        // Validate approver role
        if (approverRole != UserRole.MANAGER && approverRole != UserRole.RELATIONSHIP_OFFICER 
            && approverRole != UserRole.SYSTEM_ADMIN) {
            return ApprovalResult.failure("Insufficient privileges for user approval");
        }

        Optional<UserProfileEntity> profileOpt = userProfileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            return ApprovalResult.failure("User profile not found");
        }

        UserProfileEntity profile = profileOpt.get();
        if (profile.getStatus() != UserStatus.PENDING) {
            return ApprovalResult.failure("User is not in PENDING status");
        }

        // Approve user
        profile.setStatus(UserStatus.ACTIVE);
        profile.setApprovedBy(approverId);
        profile.setApprovedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());

        userProfileRepository.save(profile);

        log.info("User approved: {} ({}) by {}", profile.getEmail(), userId, approverRole);

        return ApprovalResult.success(userId, "User approved and activated");
    }

    /**
     * Update user status (suspend/activate).
     */
    public ApprovalResult updateUserStatus(UUID userId, UserStatus newStatus, UserRole updaterRole) {
        if (updaterRole != UserRole.SYSTEM_ADMIN && updaterRole != UserRole.MANAGER) {
            return ApprovalResult.failure("Insufficient privileges to update user status");
        }

        Optional<UserProfileEntity> profileOpt = userProfileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            return ApprovalResult.failure("User profile not found");
        }

        UserProfileEntity profile = profileOpt.get();
        profile.setStatus(newStatus);
        profile.setUpdatedAt(Instant.now());

        userProfileRepository.save(profile);

        log.info("User status updated: {} ({}) -> {} by {}", profile.getEmail(), userId, newStatus, updaterRole);

        return ApprovalResult.success(userId, "User status updated to " + newStatus);
    }

    /**
     * Get user profile by user ID.
     */
    @Transactional(readOnly = true)
    public Optional<UserProfileEntity> getUserProfile(UUID userId) {
        return userProfileRepository.findByUserId(userId);
    }

    /**
     * Get user status by user ID.
     */
    @Transactional(readOnly = true)
    public UserStatus getUserStatus(UUID userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfileEntity::getStatus)
                .orElse(null);
    }

    /**
     * Check if user is active.
     */
    @Transactional(readOnly = true)
    public boolean isActiveUser(UUID userId) {
        return getUserProfile(userId)
                .map(UserProfileEntity::isActive)
                .orElse(false);
    }

    /**
     * Result of onboarding operation.
     */
    public record OnboardingResult(
            UUID userId,
            boolean success,
            String message
    ) {
        public static OnboardingResult success(UUID userId, String message) {
            return new OnboardingResult(userId, true, message);
        }

        public static OnboardingResult failure(String reason) {
            return new OnboardingResult(null, false, reason);
        }
    }
}
