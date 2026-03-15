package com.neobank.auth.internal;

import com.neobank.auth.*;
import com.neobank.auth.api.AuthApi;
import com.neobank.auth.api.UserCreatedEvent;
import com.neobank.branches.internal.BranchService;
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

        // Create new user entity
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername(request.username());
        userEntity.setEmail(request.email());
        userEntity.setPasswordHash(passwordEncoder.encode(request.password()));
        userEntity.setRole(UserRole.CUSTOMER_RETAIL);
        userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));
        // Auto-assign to Head Office branch
        userEntity.setBranchId(branchService.getHeadOffice().getId());
        userEntity.setCreatedAt(Instant.now());

        // Save user
        userRepository.save(userEntity);

        // Publish UserCreatedEvent for cross-module integration
        eventPublisher.publishEvent(UserCreatedEvent.of(userId, request.username(), userEntity.getBranchId()));

        log.info("User registered: {} ({})", request.username(), userId);

        return RegistrationResult.success(userId);
    }

    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        try {
            // Get user and verify password manually
            UserEntity user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                return AuthenticationResult.failure("Invalid username or password");
            }

            // Generate JWT token
            String token = jwtService.generateToken(user.getId(), user.getUsername());

            // Update last login
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            log.info("User authenticated: {} ({})", request.username(), user.getId());

            return AuthenticationResult.success(user.getId(), token, 86400L);

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
}
