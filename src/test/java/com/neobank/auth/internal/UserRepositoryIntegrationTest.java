package com.neobank.auth.internal;

import com.neobank.auth.UserRole;
import com.neobank.auth.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Save and Retrieve")
    class SaveAndRetrieveTests {

        @Test
        @DisplayName("Should save and retrieve user by ID")
        void shouldSaveAndRetrieveUserById() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);

            // When
            UserEntity saved = userRepository.save(user);
            UserEntity retrieved = userRepository.findById(saved.getId()).orElse(null);

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getUsername()).isEqualTo(user.getUsername());
            assertThat(retrieved.getEmail()).isEqualTo(user.getEmail());
        }

        @Test
        @DisplayName("Should save user with PENDING status")
        void shouldSaveUserWithPendingStatus() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setStatus(UserStatus.PENDING);

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getStatus()).isEqualTo(UserStatus.PENDING);
        }

        @Test
        @DisplayName("Should save user with ACTIVE status")
        void shouldSaveUserWithActiveStatus() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setStatus(UserStatus.ACTIVE);

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should save user with SUSPENDED status")
        void shouldSaveUserWithSuspendedStatus() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setStatus(UserStatus.SUSPENDED);

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        }

        @Test
        @DisplayName("Should save user with ROLE_GUEST")
        void shouldSaveUserWithRoleGuest() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setRole(UserRole.ROLE_GUEST);
            user.setRoles(List.of(UserRole.ROLE_GUEST));

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getRole()).isEqualTo(UserRole.ROLE_GUEST);
        }

        @Test
        @DisplayName("Should save user with CUSTOMER_RETAIL role")
        void shouldSaveUserWithCustomerRetailRole() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setRole(UserRole.CUSTOMER_RETAIL);
            user.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getRole()).isEqualTo(UserRole.CUSTOMER_RETAIL);
        }

        @Test
        @DisplayName("Should save user with multiple roles")
        void shouldSaveUserWithMultipleRoles() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setRole(UserRole.MANAGER);
            user.setRoles(List.of(UserRole.MANAGER, UserRole.TELLER, UserRole.AUDITOR));

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getRoles()).hasSize(3);
            assertThat(saved.getRoles()).contains(UserRole.MANAGER, UserRole.TELLER, UserRole.AUDITOR);
        }

        @Test
        @DisplayName("Should save user with mustChangePassword flag")
        void shouldSaveUserWithMustChangePasswordFlag() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setMustChangePassword(true);

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.isMustChangePassword()).isTrue();
        }

        @Test
        @DisplayName("Should save user with last login timestamp")
        void shouldSaveUserWithLastLoginTimestamp() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            Instant lastLoginAt = Instant.now();
            user.setLastLoginAt(lastLoginAt);

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getLastLoginAt()).isEqualTo(lastLoginAt);
        }

        @Test
        @DisplayName("Should save user with metadata JSON")
        void shouldSaveUserWithMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setMetadataJson("{\"key\":\"value\"}");

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getMetadataJson()).isEqualTo("{\"key\":\"value\"}");
        }
    }

    @Nested
    @DisplayName("Find By Username")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            // Given
            String username = "testuser";
            UserEntity user = createTestUser(UUID.randomUUID());
            user.setUsername(username);
            userRepository.save(user);

            // When
            Optional<UserEntity> result = userRepository.findByUsername(username);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should return empty when user not found by username")
        void shouldReturnEmptyWhenUserNotFoundByUsername() {
            // Given
            String nonExistentUsername = "nonexistent";

            // When
            Optional<UserEntity> result = userRepository.findByUsername(nonExistentUsername);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find user by username with special characters")
        void shouldFindUserByUsernameWithSpecialCharacters() {
            // Given
            String username = "test+user@example.com";
            UserEntity user = createTestUser(UUID.randomUUID());
            user.setUsername(username);
            userRepository.save(user);

            // When
            Optional<UserEntity> result = userRepository.findByUsername(username);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should find user by username with unicode")
        void shouldFindUserByUsernameWithUnicode() {
            // Given
            String username = "用户 测试";
            UserEntity user = createTestUser(UUID.randomUUID());
            user.setUsername(username);
            userRepository.save(user);

            // When
            Optional<UserEntity> result = userRepository.findByUsername(username);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo(username);
        }
    }

    @Nested
    @DisplayName("Find By Email")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Given
            String email = "test@example.com";
            UserEntity user = createTestUser(UUID.randomUUID());
            user.setEmail(email);
            userRepository.save(user);

            // When
            Optional<UserEntity> result = userRepository.findByEmail(email);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should return empty when user not found by email")
        void shouldReturnEmptyWhenUserNotFoundByEmail() {
            // Given
            String nonExistentEmail = "nonexistent@example.com";

            // When
            Optional<UserEntity> result = userRepository.findByEmail(nonExistentEmail);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find user by email with special characters")
        void shouldFindUserByEmailWithSpecialCharacters() {
            // Given
            String email = "test+user@example.com";
            UserEntity user = createTestUser(UUID.randomUUID());
            user.setEmail(email);
            userRepository.save(user);

            // When
            Optional<UserEntity> result = userRepository.findByEmail(email);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("Exists By Username")
    class ExistsByUsernameTests {

        @Test
        @DisplayName("Should return true when username exists")
        void shouldReturnTrueWhenUsernameExists() {
            // Given
            String username = "testuser";
            UserEntity user = createTestUser(UUID.randomUUID());
            user.setUsername(username);
            userRepository.save(user);

            // When
            boolean exists = userRepository.existsByUsername(username);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when username does not exist")
        void shouldReturnFalseWhenUsernameDoesNotExist() {
            // Given
            String nonExistentUsername = "nonexistent";

            // When
            boolean exists = userRepository.existsByUsername(nonExistentUsername);

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Exists By Email")
    class ExistsByEmailTests {

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // Given
            String email = "test@example.com";
            UserEntity user = createTestUser(UUID.randomUUID());
            user.setEmail(email);
            userRepository.save(user);

            // When
            boolean exists = userRepository.existsByEmail(email);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // Given
            String nonExistentEmail = "nonexistent@example.com";

            // When
            boolean exists = userRepository.existsByEmail(nonExistentEmail);

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Unique Constraints")
    class UniqueConstraintsTests {

        @Test
        @DisplayName("Should enforce unique username constraint")
        void shouldEnforceUniqueUsernameConstraint() {
            // Given
            String username = "duplicateuser";
            UserEntity user1 = createTestUser(UUID.randomUUID());
            user1.setUsername(username);
            userRepository.save(user1);

            UserEntity user2 = createTestUser(UUID.randomUUID());
            user2.setUsername(username);

            // When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    org.springframework.dao.DataIntegrityViolationException.class,
                    () -> userRepository.save(user2)
            );
        }

        @Test
        @DisplayName("Should enforce unique email constraint")
        void shouldEnforceUniqueEmailConstraint() {
            // Given
            String email = "duplicate@example.com";
            UserEntity user1 = createTestUser(UUID.randomUUID());
            user1.setEmail(email);
            userRepository.save(user1);

            UserEntity user2 = createTestUser(UUID.randomUUID());
            user2.setEmail(email);

            // When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    org.springframework.dao.DataIntegrityViolationException.class,
                    () -> userRepository.save(user2)
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null metadata JSON")
        void shouldHandleNullMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setMetadataJson(null);

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getMetadataJson()).isNull();
        }

        @Test
        @DisplayName("Should handle empty metadata JSON")
        void shouldHandleEmptyMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setMetadataJson("{}");

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getMetadataJson()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle long username")
        void shouldHandleLongUsername() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setUsername("a".repeat(100));

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getUsername()).hasSize(100);
        }

        @Test
        @DisplayName("Should handle long email")
        void shouldHandleLongEmail() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setEmail("a".repeat(200) + "@example.com");

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getEmail()).hasSize(212);
        }

        @Test
        @DisplayName("Should handle user with empty roles list")
        void shouldHandleUserWithEmptyRolesList() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user.setRoles(List.of());

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getRoles()).isEmpty();
        }

        @Test
        @DisplayName("Should handle deleting users")
        void shouldHandleDeletingUsers() {
            // Given
            UUID id = UUID.randomUUID();
            UserEntity user = createTestUser(id);
            user = userRepository.save(user);

            // When
            userRepository.delete(user);

            // Then
            assertThat(userRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should handle deleting all users")
        void shouldHandleDeletingAllUsers() {
            // Given
            List<UserEntity> users = List.of(
                    createTestUser(UUID.randomUUID()),
                    createTestUser(UUID.randomUUID()),
                    createTestUser(UUID.randomUUID())
            );
            userRepository.saveAll(users);

            // When
            userRepository.deleteAll();

            // Then
            assertThat(userRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should handle user with all fields set")
        void shouldHandleUserWithAllFieldsSet() {
            // Given
            UUID id = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(id);
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setPasswordHash("encodedPassword");
            user.setRole(UserRole.CUSTOMER_RETAIL);
            user.setBranchId(branchId);
            user.setStatus(UserStatus.ACTIVE);
            user.setMustChangePassword(true);
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            user.setLastLoginAt(Instant.now());
            user.setMetadataJson("{\"key\":\"value\"}");
            user.setRoles(List.of(UserRole.CUSTOMER_RETAIL, UserRole.TELLER));

            // When
            UserEntity saved = userRepository.save(user);

            // Then
            assertThat(saved.getId()).isEqualTo(id);
            assertThat(saved.getUsername()).isEqualTo("testuser");
            assertThat(saved.getEmail()).isEqualTo("test@example.com");
            assertThat(saved.getRole()).isEqualTo(UserRole.CUSTOMER_RETAIL);
            assertThat(saved.getBranchId()).isEqualTo(branchId);
            assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(saved.isMustChangePassword()).isTrue();
            assertThat(saved.getRoles()).hasSize(2);
        }
    }

    /**
     * Helper method to create a test user.
     */
    private UserEntity createTestUser(UUID id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername("testuser" + id);
        user.setEmail("test" + id + "@example.com");
        user.setPasswordHash("encodedPassword");
        user.setRole(UserRole.CUSTOMER_RETAIL);
        user.setRoles(List.of(UserRole.CUSTOMER_RETAIL));
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(false);
        user.setCreatedAt(Instant.now());
        return user;
    }
}
