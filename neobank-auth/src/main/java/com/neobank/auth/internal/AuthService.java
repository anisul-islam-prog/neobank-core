package com.neobank.auth.internal;

import com.neobank.auth.*;
import com.neobank.auth.api.AuthApi;
import com.neobank.auth.api.UserCreatedEvent;
import com.neobank.core.branches.BranchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Internal auth service implementing user registration and authentication.
 */
@Service
@Transactional
class AuthService implements AuthApi {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;
    private final BranchService branchService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, ApplicationEventPublisher eventPublisher,
                       BranchService branchService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
        this.branchService = branchService;
    }

    @Override
    public RegistrationResult register(RegistrationRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            return RegistrationResult.failure("Username already exists: " + request.username());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            return RegistrationResult.failure("Email already registered: " + request.email());
        }

        // Create new user entity with PENDING status and ROLE_GUEST
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername(request.username());
        userEntity.setEmail(request.email());
        userEntity.setPasswordHash(passwordEncoder.encode(request.password()));
        userEntity.setRole(UserRole.ROLE_GUEST);
        userEntity.setRoles(List.of(UserRole.ROLE_GUEST));
        userEntity.setStatus(UserStatus.PENDING);
        userEntity.setMustChangePassword(false);
        // Auto-assign to Head Office branch
        userEntity.setBranchId(branchService.getHeadOffice().getId());
        userEntity.setCreatedAt(Instant.now());

        // Save user
        userRepository.save(userEntity);

        // Publish UserCreatedEvent for cross-module integration
        eventPublisher.publishEvent(UserCreatedEvent.of(userId, request.username(), userEntity.getBranchId()));

        log.info("User registered (PENDING approval): {} ({})", request.username(), userId);

        return RegistrationResult.success(userId);
    }

    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        try {
            // Get user and verify password manually
            UserEntity user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check user status - reject PENDING or SUSPENDED users
            if (user.getStatus() == UserStatus.PENDING) {
                log.warn("Login attempt by PENDING user: {}", request.username());
                return AuthenticationResult.forbidden("Account pending approval. Please contact support.");
            }
            if (user.getStatus() == UserStatus.SUSPENDED) {
                log.warn("Login attempt by SUSPENDED user: {}", request.username());
                return AuthenticationResult.forbidden("Account suspended. Please contact support.");
            }

            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                return AuthenticationResult.failure("Invalid username or password");
            }

            // Generate JWT token
            String token = jwtService.generateToken(user.getId(), user.getUsername());

            // Update last login
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            log.info("User authenticated: {} ({}) - Status: {}", request.username(), user.getId(), user.getStatus());

            return AuthenticationResult.success(user.getId(), token, 86400L, user.isMustChangePassword(), user.getStatus());

        } catch (Exception e) {
            log.warn("Authentication failed for {}: {}", request.username(), e.getMessage());
            return AuthenticationResult.failure("Invalid username or password");
        }
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        return jwtService.extractUserId(token);
    }

    /**
     * Get user by ID.
     * Package-private for internal use.
     */
    UserEntity getUserById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Onboard a new staff user with specific role.
     * Used by Admin/RO to create staff accounts.
     * Sets mustChangePassword = true to force password reset on first login.
     */
    @Transactional
    public RegistrationResult onboardStaff(StaffOnboardingRequest request, UserRole createdBy) {
        // Validate that creator has sufficient privileges
        if (createdBy != UserRole.SYSTEM_ADMIN && createdBy != UserRole.MANAGER && createdBy != UserRole.RELATIONSHIP_OFFICER) {
            return RegistrationResult.failure("Insufficient privileges for staff onboarding");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            return RegistrationResult.failure("Username already exists: " + request.username());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            return RegistrationResult.failure("Email already registered: " + request.email());
        }

        // Validate role assignment (SYSTEM_ADMIN can assign any role, others limited)
        if (createdBy != UserRole.SYSTEM_ADMIN) {
            if (request.role() == UserRole.SYSTEM_ADMIN || request.role() == UserRole.AUDITOR) {
                return RegistrationResult.failure("Insufficient privileges to assign " + request.role());
            }
        }

        // Create new staff user with ACTIVE status and mustChangePassword = true
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername(request.username());
        userEntity.setEmail(request.email());
        userEntity.setPasswordHash(passwordEncoder.encode(request.password()));
        userEntity.setRole(request.role());
        userEntity.setRoles(List.of(request.role()));
        userEntity.setStatus(UserStatus.ACTIVE);
        userEntity.setMustChangePassword(true);
        userEntity.setBranchId(request.branchId() != null ? request.branchId() : branchService.getHeadOffice().getId());
        userEntity.setCreatedAt(Instant.now());

        userRepository.save(userEntity);

        log.info("Staff user onboarded: {} ({}) with role {} by {}", request.username(), userId, request.role(), createdBy);

        return RegistrationResult.success(userId);
    }

    /**
     * Approve a pending user (promote from ROLE_GUEST to CUSTOMER_RETAIL).
     * Only accessible by MANAGER or RELATIONSHIP_OFFICER.
     */
    @Transactional
    public ApprovalResult approveUser(UUID userId, UserRole approvedBy) {
        if (approvedBy != UserRole.MANAGER && approvedBy != UserRole.RELATIONSHIP_OFFICER && approvedBy != UserRole.SYSTEM_ADMIN) {
            return ApprovalResult.failure("Insufficient privileges for user approval");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != UserStatus.PENDING) {
            return ApprovalResult.failure("User is not in PENDING status");
        }

        if (user.getRole() != UserRole.ROLE_GUEST) {
            return ApprovalResult.failure("User role is not ROLE_GUEST");
        }

        // Approve user: set status to ACTIVE and promote to CUSTOMER_RETAIL
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.CUSTOMER_RETAIL);
        user.setRoles(List.of(UserRole.CUSTOMER_RETAIL));
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        log.info("User approved: {} ({}) by {}", user.getUsername(), userId, approvedBy);

        return ApprovalResult.success(userId, "User approved and activated");
    }

    /**
     * Approve a staff user (activate MANAGER, TELLER, or RO account).
     * Only accessible by SYSTEM_ADMIN.
     */
    @Transactional
    public ApprovalResult approveStaff(UUID userId, UserRole approvedBy) {
        if (approvedBy != UserRole.SYSTEM_ADMIN) {
            return ApprovalResult.failure("Only SYSTEM_ADMIN can approve staff accounts");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != UserStatus.PENDING) {
            return ApprovalResult.failure("User is not in PENDING status");
        }

        // Validate staff role
        if (user.getRole() != UserRole.TELLER && user.getRole() != UserRole.MANAGER && 
            user.getRole() != UserRole.RELATIONSHIP_OFFICER && user.getRole() != UserRole.AUDITOR) {
            return ApprovalResult.failure("Invalid staff role for approval");
        }

        // Approve staff: set status to ACTIVE
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        log.info("Staff user approved: {} ({}) by {}", user.getUsername(), userId, approvedBy);

        return ApprovalResult.success(userId, "Staff user approved and activated");
    }

    /**
     * Update user status (e.g., suspend/activate).
     */
    @Transactional
    public ApprovalResult updateUserStatus(UUID userId, UserStatus newStatus, UserRole updatedBy) {
        if (updatedBy != UserRole.SYSTEM_ADMIN && updatedBy != UserRole.MANAGER) {
            return ApprovalResult.failure("Insufficient privileges to update user status");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(newStatus);
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        log.info("User status updated: {} ({}) -> {} by {}", user.getUsername(), userId, newStatus, updatedBy);

        return ApprovalResult.success(userId, "User status updated to " + newStatus);
    }
}
