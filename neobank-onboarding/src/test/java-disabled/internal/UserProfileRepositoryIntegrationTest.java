package com.neobank.onboarding.internal;

import com.neobank.onboarding.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Integration test for UserProfileRepository using Testcontainers.
 * Tests repository queries against a real PostgreSQL database.
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("UserProfileRepository Integration Tests")
class UserProfileRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private UserProfileRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("Save and Retrieve")
    class SaveAndRetrieveTests {

        @Test
        @DisplayName("Should save and retrieve user profile by ID")
        void shouldSaveAndRetrieveUserProfileById() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);

            // When
            UserProfileEntity saved = repository.save(profile);
            UserProfileEntity retrieved = repository.findById(saved.getId()).orElse(null);

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getEmail()).isEqualTo(profile.getEmail());
            assertThat(retrieved.getStatus()).isEqualTo(profile.getStatus());
        }

        @Test
        @DisplayName("Should save user profile with PENDING status")
        void shouldSaveUserProfileWithPendingStatus() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setStatus(UserStatus.PENDING);

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getStatus()).isEqualTo(UserStatus.PENDING);
        }

        @Test
        @DisplayName("Should save user profile with ACTIVE status")
        void shouldSaveUserProfileWithActiveStatus() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setStatus(UserStatus.ACTIVE);

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should save user profile with SUSPENDED status")
        void shouldSaveUserProfileWithSuspendedStatus() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setStatus(UserStatus.SUSPENDED);

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        }

        @Test
        @DisplayName("Should save user profile with KYC verified")
        void shouldSaveUserProfileWithKycVerified() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setKycVerified(true);
            profile.setKycDocumentUrl("https://storage.example.com/kyc/doc.pdf");
            profile.setKycVerifiedAt(Instant.now());

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.isKycVerified()).isTrue();
            assertThat(saved.getKycDocumentUrl()).isEqualTo("https://storage.example.com/kyc/doc.pdf");
            assertThat(saved.getKycVerifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should save user profile with approval information")
        void shouldSaveUserProfileWithApprovalInformation() {
            // Given
            UUID id = UUID.randomUUID();
            UUID approvedBy = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setStatus(UserStatus.ACTIVE);
            profile.setApprovedBy(approvedBy);
            profile.setApprovedAt(Instant.now());

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getApprovedBy()).isEqualTo(approvedBy);
            assertThat(saved.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should save user profile with metadata JSON")
        void shouldSaveUserProfileWithMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setMetadataJson("{\"source\":\"web_registration\"}");

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getMetadataJson()).isEqualTo("{\"source\":\"web_registration\"}");
        }
    }

    @Nested
    @DisplayName("Find By User ID")
    class FindByUserIdTests {

        @Test
        @DisplayName("Should find user profile by user ID")
        void shouldFindUserProfileByUserId() {
            // Given
            UUID userId = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(UUID.randomUUID());
            profile.setUserId(userId);
            repository.save(profile);

            // When
            Optional<UserProfileEntity> result = repository.findByUserId(userId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should return empty when user profile not found by user ID")
        void shouldReturnEmptyWhenUserProfileNotFoundByUserId() {
            // Given
            UUID nonExistentUserId = UUID.randomUUID();

            // When
            Optional<UserProfileEntity> result = repository.findByUserId(nonExistentUserId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Email")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find user profile by email")
        void shouldFindUserProfileByEmail() {
            // Given
            String email = "test@example.com";
            UserProfileEntity profile = createTestProfile(UUID.randomUUID());
            profile.setEmail(email);
            repository.save(profile);

            // When
            Optional<UserProfileEntity> result = repository.findByEmail(email);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should return empty when user profile not found by email")
        void shouldReturnEmptyWhenUserProfileNotFoundByEmail() {
            // Given
            String nonExistentEmail = "nonexistent@example.com";

            // When
            Optional<UserProfileEntity> result = repository.findByEmail(nonExistentEmail);

            // Then
            assertThat(result).isEmpty();
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
            UserProfileEntity profile = createTestProfile(UUID.randomUUID());
            profile.setEmail(email);
            repository.save(profile);

            // When
            boolean exists = repository.existsByEmail(email);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // Given
            String nonExistentEmail = "nonexistent@example.com";

            // When
            boolean exists = repository.existsByEmail(nonExistentEmail);

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Exists By User ID")
    class ExistsByUserIdTests {

        @Test
        @DisplayName("Should return true when user ID exists")
        void shouldReturnTrueWhenUserIdExists() {
            // Given
            UUID userId = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(UUID.randomUUID());
            profile.setUserId(userId);
            repository.save(profile);

            // When
            boolean exists = repository.existsByUserId(userId);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when user ID does not exist")
        void shouldReturnFalseWhenUserIdDoesNotExist() {
            // Given
            UUID nonExistentUserId = UUID.randomUUID();

            // When
            boolean exists = repository.existsByUserId(nonExistentUserId);

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Update Status By User ID")
    class UpdateStatusByUserIdTests {

        @Test
        @DisplayName("Should update user status by user ID")
        void shouldUpdateUserStatusByUserId() {
            // Given
            UUID userId = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.PENDING);
            repository.save(profile);

            // When
            int updated = repository.updateStatusByUserId(userId, UserStatus.SUSPENDED);

            // Then
            assertThat(updated).isEqualTo(1);
            Optional<UserProfileEntity> result = repository.findByUserId(userId);
            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(UserStatus.SUSPENDED);
        }

        @Test
        @DisplayName("Should return 0 when user ID not found for status update")
        void shouldReturn0WhenUserIdNotFoundForStatusUpdate() {
            // Given
            UUID nonExistentUserId = UUID.randomUUID();

            // When
            int updated = repository.updateStatusByUserId(nonExistentUserId, UserStatus.SUSPENDED);

            // Then
            assertThat(updated).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Approve User")
    class ApproveUserTests {

        @Test
        @DisplayName("Should approve user by user ID")
        void shouldApproveUserByUserId() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.PENDING);
            repository.save(profile);

            // When
            int approved = repository.approveUser(userId, approverId);

            // Then
            assertThat(approved).isEqualTo(1);
            Optional<UserProfileEntity> result = repository.findByUserId(userId);
            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(result.get().getApprovedBy()).isEqualTo(approverId);
            assertThat(result.get().getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should return 0 when user ID not found for approval")
        void shouldReturn0WhenUserIdNotFoundForApproval() {
            // Given
            UUID nonExistentUserId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();

            // When
            int approved = repository.approveUser(nonExistentUserId, approverId);

            // Then
            assertThat(approved).isEqualTo(0);
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
            UserProfileEntity profile = createTestProfile(id);
            profile.setMetadataJson(null);

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getMetadataJson()).isNull();
        }

        @Test
        @DisplayName("Should handle empty metadata JSON")
        void shouldHandleEmptyMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setMetadataJson("{}");

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getMetadataJson()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle long email")
        void shouldHandleLongEmail() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setEmail("a".repeat(200) + "@example.com");

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getEmail()).hasSize(212);
        }

        @Test
        @DisplayName("Should handle long KYC document URL")
        void shouldHandleLongKycDocumentUrl() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setKycDocumentUrl("https://storage.example.com/" + "a".repeat(400) + ".pdf");

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getKycDocumentUrl()).hasSizeGreaterThan(400);
        }

        @Test
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setEmail("test+user@example.com");

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getEmail()).isEqualTo("test+user@example.com");
        }

        @Test
        @DisplayName("Should handle email with unicode characters")
        void shouldHandleEmailWithUnicodeCharacters() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setEmail("用户@example.com");

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getEmail()).isEqualTo("用户@example.com");
        }

        @Test
        @DisplayName("Should handle deleting user profiles")
        void shouldHandleDeletingUserProfiles() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile = repository.save(profile);

            // When
            repository.delete(profile);

            // Then
            assertThat(repository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should handle deleting all user profiles")
        void shouldHandleDeletingAllUserProfiles() {
            // Given
            List<UserProfileEntity> profiles = List.of(
                    createTestProfile(UUID.randomUUID()),
                    createTestProfile(UUID.randomUUID()),
                    createTestProfile(UUID.randomUUID())
            );
            repository.saveAll(profiles);

            // When
            repository.deleteAll();

            // Then
            assertThat(repository.findAll()).isEmpty();
        }
    }

    /**
     * Helper method to create a test user profile.
     */
    private UserProfileEntity createTestProfile(UUID id) {
        UserProfileEntity profile = new UserProfileEntity();
        profile.setId(id);
        profile.setUserId(UUID.randomUUID());
        profile.setEmail("test" + id + "@example.com");
        profile.setStatus(UserStatus.PENDING);
        profile.setMustChangePassword(false);
        profile.setKycVerified(false);
        profile.setCreatedAt(Instant.now());
        return profile;
    }
}
