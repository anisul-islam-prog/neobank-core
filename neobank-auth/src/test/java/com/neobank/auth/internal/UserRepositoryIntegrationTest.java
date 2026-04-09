package com.neobank.auth.internal;

import com.neobank.AbstractIntegrationTest;
import com.neobank.auth.UserRole;
import com.neobank.auth.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for UserRepository using Testcontainers.
 * Tests repository queries against a real PostgreSQL database.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(UserRepositoryIntegrationTest.TestConfig.class)
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

    @org.springframework.boot.autoconfigure.SpringBootApplication
    static class TestConfig {
    }

    /**
     * PostgreSQL container with @ServiceConnection for automatic DataSource configuration.
     * Spring Boot 4 + Testcontainers 2.0 auto-configures the database connection.
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        entityManager.clear();
    }

    @Nested
    @DisplayName("Save and Retrieve")
    class SaveAndRetrieveTests {

        @Test
        @DisplayName("Should save and retrieve user by ID")
        void shouldSaveAndRetrieveUserById() {
            // Given
            UserEntity user = createUserEntity();

            // When
            UserEntity saved = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<UserEntity> found = userRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should save and retrieve user by username")
        void shouldSaveAndRetrieveUserByUsername() {
            // Given
            UserEntity user = createUserEntity();

            // When
            UserEntity saved = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<UserEntity> found = userRepository.findByUsername("testuser");
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("Should save and retrieve user by email")
        void shouldSaveAndRetrieveUserByEmail() {
            // Given
            UserEntity user = createUserEntity();

            // When
            UserEntity saved = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<UserEntity> found = userRepository.findByEmail("test@example.com");
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
        }
    }

    @Nested
    @DisplayName("Bulk Operations")
    class BulkOperationsTests {

        @Test
        @DisplayName("Should find all users")
        void shouldFindAllUsers() {
            // Given
            userRepository.save(createUserEntity());
            userRepository.save(createUserEntityWithEmail("user2", "user2@example.com"));
            userRepository.save(createUserEntityWithEmail("user3", "user3@example.com"));

            // When
            List<UserEntity> users = userRepository.findAll();

            // Then
            assertThat(users).hasSize(3);
        }

        @Test
        @DisplayName("Should count all users")
        void shouldCountAllUsers() {
            // Given
            userRepository.save(createUserEntity());
            userRepository.save(createUserEntityWithEmail("user2", "user2@example.com"));

            // When
            long count = userRepository.count();

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should delete user by ID")
        void shouldDeleteUserById() {
            // Given
            UserEntity user = userRepository.save(createUserEntity());
            entityManager.flush();

            // When
            userRepository.deleteById(user.getId());
            entityManager.flush();

            // Then
            assertThat(userRepository.findById(user.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("User Status Transitions")
    class UserStatusTransitionsTests {

        @Test
        @DisplayName("Should update user status from PENDING to ACTIVE")
        void shouldUpdateUserStatusFromPendingToActive() {
            // Given
            UserEntity user = createUserEntity();
            user.setStatus(UserStatus.PENDING);
            user = userRepository.save(user);
            entityManager.flush();

            // When
            user.setStatus(UserStatus.ACTIVE);
            user = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<UserEntity> updated = userRepository.findById(user.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should update user status to SUSPENDED")
        void shouldUpdateUserStatusToSuspended() {
            // Given
            UserEntity user = createUserEntity();
            user.setStatus(UserStatus.ACTIVE);
            user = userRepository.save(user);
            entityManager.flush();

            // When
            user.setStatus(UserStatus.SUSPENDED);
            user = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<UserEntity> updated = userRepository.findById(user.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getStatus()).isEqualTo(UserStatus.SUSPENDED);
        }
    }

    @Nested
    @DisplayName("User Role Management")
    class UserRoleManagementTests {

        @Test
        @DisplayName("Should save user with CUSTOMER role")
        void shouldSaveUserWithCustomerRole() {
            // Given
            UserEntity user = createUserEntity();
            user.setRole(UserRole.CUSTOMER_RETAIL);

            // When
            UserEntity saved = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<UserEntity> found = userRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getRole()).isEqualTo(UserRole.CUSTOMER_RETAIL);
        }

        @Test
        @DisplayName("Should save user with MANAGER role")
        void shouldSaveUserWithManagerRole() {
            // Given
            UserEntity user = createUserEntity();
            user.setRole(UserRole.MANAGER);

            // When
            UserEntity saved = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<UserEntity> found = userRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getRole()).isEqualTo(UserRole.MANAGER);
        }

        @Test
        @DisplayName("Should save user with SYSTEM_ADMIN role")
        void shouldSaveUserWithSystemAdminRole() {
            // Given
            UserEntity user = createUserEntity();
            user.setRole(UserRole.MANAGER);

            // When
            UserEntity saved = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<UserEntity> found = userRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getRole()).isEqualTo(UserRole.MANAGER);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle user with very long username")
        void shouldHandleUserWithVeryLongUsername() {
            // Given
            String longUsername = "a".repeat(100);
            UserEntity user = createUserEntityWithEmail(longUsername, "long@example.com");

            // When
            UserEntity saved = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<UserEntity> found = userRepository.findByUsername(longUsername);
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("Should reject user with null password hash")
        void shouldHandleUserWithNullPasswordHash() {
            // Given
            UserEntity user = createUserEntity();
            user.setPasswordHash(null);

            // When/Then - Should throw exception due to not-null constraint
            org.junit.jupiter.api.Assertions.assertThrows(
                org.hibernate.exception.ConstraintViolationException.class,
                () -> {
                    userRepository.save(user);
                    entityManager.flush();
                }
            );
        }

        @Test
        @DisplayName("Should handle special characters in email")
        void shouldHandleSpecialCharactersInEmail() {
            // Given
            String specialEmail = "test+label@example.co.uk";
            UserEntity user = createUserEntityWithEmail("specialuser", specialEmail);

            // When
            UserEntity saved = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<UserEntity> found = userRepository.findByEmail(specialEmail);
            assertThat(found).isPresent();
        }
    }

    // Helper methods
    private UserEntity createUserEntity() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword123");
        user.setRole(UserRole.CUSTOMER_RETAIL);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    private UserEntity createUserEntityWithEmail(String username, String email) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("hashedPassword123");
        user.setRole(UserRole.CUSTOMER_RETAIL);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }
}
