package com.neobank.auth.internal;

import com.neobank.auth.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for checking user status from other modules.
 * Used by loans, cards, and other modules to enforce access control.
 */
@Service
@Transactional(readOnly = true)
public class UserStatusChecker {

    private final UserRepository userRepository;

    public UserStatusChecker(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Check if a user is ACTIVE.
     * 
     * @param userId the user ID to check
     * @return true if user exists and has ACTIVE status
     */
    public boolean isActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElse(false);
    }

    /**
     * Get user status.
     * 
     * @param userId the user ID
     * @return the user's status, or null if user not found
     */
    public UserStatus getUserStatus(UUID userId) {
        return userRepository.findById(userId)
                .map(UserEntity::getStatus)
                .orElse(null);
    }

    /**
     * Check if user is PENDING.
     */
    public boolean isPendingUser(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getStatus() == UserStatus.PENDING)
                .orElse(false);
    }

    /**
     * Check if user is SUSPENDED.
     */
    public boolean isSuspendedUser(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getStatus() == UserStatus.SUSPENDED)
                .orElse(false);
    }
}
