package com.neobank.auth.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for user persistence.
 */
@Repository
interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Find user by username.
     *
     * @param username the username
     * @return optional user
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Check if username exists.
     *
     * @param username the username
     * @return true if exists
     */
    boolean existsByUsername(String username);
}
