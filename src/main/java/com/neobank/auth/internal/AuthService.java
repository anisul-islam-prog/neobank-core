package com.neobank.auth.internal;

import com.neobank.auth.*;
import com.neobank.auth.api.AuthApi;
import com.neobank.auth.api.UserCreatedEvent;
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

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RegistrationResult register(RegistrationRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            return RegistrationResult.failure("Username already exists: " + request.username());
        }

        // Create new user entity
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername(request.username());
        userEntity.setPasswordHash(passwordEncoder.encode(request.password()));
        userEntity.setRole(UserRole.USER);
        userEntity.setRoles(List.of(UserRole.USER));
        userEntity.setCreatedAt(Instant.now());

        // Save user
        userRepository.save(userEntity);

        // Publish UserCreatedEvent for cross-module integration
        eventPublisher.publishEvent(UserCreatedEvent.of(userId, request.username()));

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
