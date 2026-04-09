package com.neobank.onboarding.internal;

import com.neobank.onboarding.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
@ContextConfiguration(classes = RepositoryTestConfig.class)
@DisplayName("UserProfileRepository Integration Tests")
class UserProfileRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private UserProfileRepository repository;

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
            profile.setKycVerifiedAt(Instant.now());

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.isKycVerified()).isTrue();
            assertThat(saved.getKycVerifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should save user profile with must change password flag")
        void shouldSaveUserProfileWithMustChangePasswordFlag() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setMustChangePassword(true);

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.isMustChangePassword()).isTrue();
        }

        @Test
        @DisplayName("Should save user profile with metadata JSON")
        void shouldSaveUserProfileWithMetadataJson() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setMetadataJson("{\"source\":\"web\"}");

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getMetadataJson()).isEqualTo("{\"source\":\"web\"}");
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
            UserProfileEntity profile = createTestProfileWithUser(UUID.randomUUID(), userId);
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
    @DisplayName("Find By Status")
    class FindByStatusTests {

        @Test
        @DisplayName("Should find all pending user profiles")
        void shouldFindAllPendingUserProfiles() {
            // Given
            UserProfileEntity profile1 = createTestProfile(UUID.randomUUID());
            profile1.setStatus(UserStatus.PENDING);
            repository.save(profile1);

            UserProfileEntity profile2 = createTestProfile(UUID.randomUUID());
            profile2.setStatus(UserStatus.PENDING);
            repository.save(profile2);

            UserProfileEntity profile3 = createTestProfile(UUID.randomUUID());
            profile3.setStatus(UserStatus.ACTIVE);
            repository.save(profile3);

            // When - Get all and filter by status
            var allProfiles = repository.findAll();
            var results = allProfiles.stream()
                    .filter(p -> p.getStatus() == UserStatus.PENDING)
                    .toList();

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(p -> p.getStatus() == UserStatus.PENDING);
        }

        @Test
        @DisplayName("Should find all active user profiles")
        void shouldFindAllActiveUserProfiles() {
            // Given
            UserProfileEntity profile1 = createTestProfile(UUID.randomUUID());
            profile1.setStatus(UserStatus.ACTIVE);
            repository.save(profile1);

            UserProfileEntity profile2 = createTestProfile(UUID.randomUUID());
            profile2.setStatus(UserStatus.ACTIVE);
            repository.save(profile2);

            // When - Get all and filter by status
            var allProfiles = repository.findAll();
            var results = allProfiles.stream()
                    .filter(p -> p.getStatus() == UserStatus.ACTIVE)
                    .toList();

            // Then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no profiles for status")
        void shouldReturnEmptyListWhenNoProfilesForStatus() {
            // When - Get all and filter by status
            var allProfiles = repository.findAll();
            var results = allProfiles.stream()
                    .filter(p -> p.getStatus() == UserStatus.PENDING)
                    .toList();

            // Then
            assertThat(results).isEmpty();
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
        @DisplayName("Should handle null KYC document URL")
        void shouldHandleNullKycDocumentUrl() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            profile.setKycDocumentUrl(null);

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getKycDocumentUrl()).isNull();
        }

        @Test
        @DisplayName("Should handle long KYC document URL")
        void shouldHandleLongKycDocumentUrl() {
            // Given
            UUID id = UUID.randomUUID();
            UserProfileEntity profile = createTestProfile(id);
            String longUrl = "https://example.com/documents/" + "a".repeat(300);
            profile.setKycDocumentUrl(longUrl);

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getKycDocumentUrl()).isEqualTo(longUrl);
        }

        @Test
        @DisplayName("Should handle user profile with all fields set")
        void shouldHandleUserProfileWithAllFieldsSet() {
            // Given
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID approvedBy = UUID.randomUUID();
            Instant now = Instant.now();
            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(id);
            profile.setUserId(userId);
            profile.setEmail("test@example.com");
            profile.setStatus(UserStatus.ACTIVE);
            profile.setMustChangePassword(true);
            profile.setKycVerified(true);
            profile.setKycDocumentUrl("https://example.com/doc.pdf");
            profile.setKycVerifiedAt(now);
            profile.setApprovedBy(approvedBy);
            profile.setApprovedAt(now);
            profile.setCreatedAt(now);
            profile.setUpdatedAt(now);
            profile.setMetadataJson("{\"source\":\"mobile\"}");

            // When
            UserProfileEntity saved = repository.save(profile);

            // Then
            assertThat(saved.getId()).isEqualTo(id);
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getEmail()).isEqualTo("test@example.com");
            assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(saved.isMustChangePassword()).isTrue();
            assertThat(saved.isKycVerified()).isTrue();
            assertThat(saved.getKycDocumentUrl()).isEqualTo("https://example.com/doc.pdf");
            assertThat(saved.getMetadataJson()).isEqualTo("{\"source\":\"mobile\"}");
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

    /**
     * Helper method to create a test user profile with specific user ID.
     */
    private UserProfileEntity createTestProfileWithUser(UUID id, UUID userId) {
        UserProfileEntity profile = createTestProfile(id);
        profile.setUserId(userId);
        return profile;
    }
}
