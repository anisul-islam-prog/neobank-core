package com.neobank.onboarding.internal;

import com.neobank.onboarding.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserProfileEntity using JUnit 5.
 * Tests entity state and field accessors.
 */
@DisplayName("UserProfileEntity Unit Tests")
class UserProfileEntityTest {

    private UserProfileEntity userProfile;

    @BeforeEach
    void setUp() {
        userProfile = new UserProfileEntity();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            UserProfileEntity entity = new UserProfileEntity();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            userProfile.setId(id);

            // Then
            assertThat(userProfile.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get user ID")
        void shouldSetAndGetUserId() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            userProfile.setUserId(userId);

            // Then
            assertThat(userProfile.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should set and get email")
        void shouldSetAndGetEmail() {
            // Given
            String email = "test@example.com";

            // When
            userProfile.setEmail(email);

            // Then
            assertThat(userProfile.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should set and get status")
        void shouldSetAndGetStatus() {
            // Given
            UserStatus status = UserStatus.PENDING;

            // When
            userProfile.setStatus(status);

            // Then
            assertThat(userProfile.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should set and get must change password")
        void shouldSetAndGetMustChangePassword() {
            // Given
            boolean mustChangePassword = true;

            // When
            userProfile.setMustChangePassword(mustChangePassword);

            // Then
            assertThat(userProfile.isMustChangePassword()).isTrue();
        }

        @Test
        @DisplayName("Should set and get KYC verified")
        void shouldSetAndGetKycVerified() {
            // Given
            boolean kycVerified = true;

            // When
            userProfile.setKycVerified(kycVerified);

            // Then
            assertThat(userProfile.isKycVerified()).isTrue();
        }

        @Test
        @DisplayName("Should set and get KYC document URL")
        void shouldSetAndGetKycDocumentUrl() {
            // Given
            String kycDocumentUrl = "https://storage.example.com/kyc/doc123.pdf";

            // When
            userProfile.setKycDocumentUrl(kycDocumentUrl);

            // Then
            assertThat(userProfile.getKycDocumentUrl()).isEqualTo(kycDocumentUrl);
        }

        @Test
        @DisplayName("Should set and get KYC verified at timestamp")
        void shouldSetAndGetKycVerifiedAtTimestamp() {
            // Given
            Instant kycVerifiedAt = Instant.now();

            // When
            userProfile.setKycVerifiedAt(kycVerifiedAt);

            // Then
            assertThat(userProfile.getKycVerifiedAt()).isEqualTo(kycVerifiedAt);
        }

        @Test
        @DisplayName("Should set and get approved by")
        void shouldSetAndGetApprovedBy() {
            // Given
            UUID approvedBy = UUID.randomUUID();

            // When
            userProfile.setApprovedBy(approvedBy);

            // Then
            assertThat(userProfile.getApprovedBy()).isEqualTo(approvedBy);
        }

        @Test
        @DisplayName("Should set and get approved at timestamp")
        void shouldSetAndGetApprovedAtTimestamp() {
            // Given
            Instant approvedAt = Instant.now();

            // When
            userProfile.setApprovedAt(approvedAt);

            // Then
            assertThat(userProfile.getApprovedAt()).isEqualTo(approvedAt);
        }

        @Test
        @DisplayName("Should set and get created at timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            // Given
            Instant createdAt = Instant.now();

            // When
            userProfile.setCreatedAt(createdAt);

            // Then
            assertThat(userProfile.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should set and get updated at timestamp")
        void shouldSetAndGetUpdatedAtTimestamp() {
            // Given
            Instant updatedAt = Instant.now();

            // When
            userProfile.setUpdatedAt(updatedAt);

            // Then
            assertThat(userProfile.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("Should set and get metadata JSON")
        void shouldSetAndGetMetadataJson() {
            // Given
            String metadataJson = "{\"source\":\"web_registration\"}";

            // When
            userProfile.setMetadataJson(metadataJson);

            // Then
            assertThat(userProfile.getMetadataJson()).isEqualTo(metadataJson);
        }
    }

    @Nested
    @DisplayName("Helper Methods")
    class HelperMethodsTests {

        @Test
        @DisplayName("Should return true for isActive when status is ACTIVE")
        void shouldReturnTrueForIsActiveWhenStatusIsActive() {
            // Given
            userProfile.setStatus(UserStatus.ACTIVE);

            // When
            boolean isActive = userProfile.isActive();

            // Then
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("Should return false for isActive when status is PENDING")
        void shouldReturnFalseForIsActiveWhenStatusIsPending() {
            // Given
            userProfile.setStatus(UserStatus.PENDING);

            // When
            boolean isActive = userProfile.isActive();

            // Then
            assertThat(isActive).isFalse();
        }

        @Test
        @DisplayName("Should return false for isActive when status is SUSPENDED")
        void shouldReturnFalseForIsActiveWhenStatusIsSuspended() {
            // Given
            userProfile.setStatus(UserStatus.SUSPENDED);

            // When
            boolean isActive = userProfile.isActive();

            // Then
            assertThat(isActive).isFalse();
        }

        @Test
        @DisplayName("Should return true for isPending when status is PENDING")
        void shouldReturnTrueForIsPendingWhenStatusIsPending() {
            // Given
            userProfile.setStatus(UserStatus.PENDING);

            // When
            boolean isPending = userProfile.isPending();

            // Then
            assertThat(isPending).isTrue();
        }

        @Test
        @DisplayName("Should return false for isPending when status is ACTIVE")
        void shouldReturnFalseForIsPendingWhenStatusIsActive() {
            // Given
            userProfile.setStatus(UserStatus.ACTIVE);

            // When
            boolean isPending = userProfile.isPending();

            // Then
            assertThat(isPending).isFalse();
        }

        @Test
        @DisplayName("Should return true for isSuspended when status is SUSPENDED")
        void shouldReturnTrueForIsSuspendedWhenStatusIsSuspended() {
            // Given
            userProfile.setStatus(UserStatus.SUSPENDED);

            // When
            boolean isSuspended = userProfile.isSuspended();

            // Then
            assertThat(isSuspended).isTrue();
        }

        @Test
        @DisplayName("Should return false for isSuspended when status is ACTIVE")
        void shouldReturnFalseForIsSuspendedWhenStatusIsActive() {
            // Given
            userProfile.setStatus(UserStatus.ACTIVE);

            // When
            boolean isSuspended = userProfile.isSuspended();

            // Then
            assertThat(isSuspended).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            userProfile.setId(null);

            // Then
            assertThat(userProfile.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null user ID")
        void shouldHandleNullUserId() {
            // When
            userProfile.setUserId(null);

            // Then
            assertThat(userProfile.getUserId()).isNull();
        }

        @Test
        @DisplayName("Should handle null email")
        void shouldHandleNullEmail() {
            // When
            userProfile.setEmail(null);

            // Then
            assertThat(userProfile.getEmail()).isNull();
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // When
            userProfile.setStatus(null);

            // Then
            assertThat(userProfile.getStatus()).isNull();
        }

        @Test
        @DisplayName("Should handle null KYC document URL")
        void shouldHandleNullKycDocumentUrl() {
            // When
            userProfile.setKycDocumentUrl(null);

            // Then
            assertThat(userProfile.getKycDocumentUrl()).isNull();
        }

        @Test
        @DisplayName("Should handle null KYC verified at timestamp")
        void shouldHandleNullKycVerifiedAtTimestamp() {
            // When
            userProfile.setKycVerifiedAt(null);

            // Then
            assertThat(userProfile.getKycVerifiedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null approved by")
        void shouldHandleNullApprovedBy() {
            // When
            userProfile.setApprovedBy(null);

            // Then
            assertThat(userProfile.getApprovedBy()).isNull();
        }

        @Test
        @DisplayName("Should handle null approved at timestamp")
        void shouldHandleNullApprovedAtTimestamp() {
            // When
            userProfile.setApprovedAt(null);

            // Then
            assertThat(userProfile.getApprovedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null created at timestamp")
        void shouldHandleNullCreatedAtTimestamp() {
            // When
            userProfile.setCreatedAt(null);

            // Then
            assertThat(userProfile.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null updated at timestamp")
        void shouldHandleNullUpdatedAtTimestamp() {
            // When
            userProfile.setUpdatedAt(null);

            // Then
            assertThat(userProfile.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null metadata JSON")
        void shouldHandleNullMetadataJson() {
            // When
            userProfile.setMetadataJson(null);

            // Then
            assertThat(userProfile.getMetadataJson()).isNull();
        }

        @Test
        @DisplayName("Should handle empty metadata JSON")
        void shouldHandleEmptyMetadataJson() {
            // When
            userProfile.setMetadataJson("{}");

            // Then
            assertThat(userProfile.getMetadataJson()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle long email")
        void shouldHandleLongEmail() {
            // When
            userProfile.setEmail("a".repeat(200) + "@example.com");

            // Then
            assertThat(userProfile.getEmail()).hasSize(212);
        }

        @Test
        @DisplayName("Should handle long KYC document URL")
        void shouldHandleLongKycDocumentUrl() {
            // When
            userProfile.setKycDocumentUrl("https://storage.example.com/" + "a".repeat(400) + ".pdf");

            // Then
            assertThat(userProfile.getKycDocumentUrl()).hasSizeGreaterThan(400);
        }

        @Test
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() {
            // When
            userProfile.setEmail("test+user@example.com");

            // Then
            assertThat(userProfile.getEmail()).isEqualTo("test+user@example.com");
        }

        @Test
        @DisplayName("Should handle email with unicode characters")
        void shouldHandleEmailWithUnicodeCharacters() {
            // When
            userProfile.setEmail("用户@example.com");

            // Then
            assertThat(userProfile.getEmail()).isEqualTo("用户@example.com");
        }

        @Test
        @DisplayName("Should handle default must change password as false")
        void shouldHandleDefaultMustChangePasswordAsFalse() {
            // Given
            UserProfileEntity entity = new UserProfileEntity();

            // Then
            assertThat(entity.isMustChangePassword()).isFalse();
        }

        @Test
        @DisplayName("Should handle default KYC verified as false")
        void shouldHandleDefaultKycVerifiedAsFalse() {
            // Given
            UserProfileEntity entity = new UserProfileEntity();

            // Then
            assertThat(entity.isKycVerified()).isFalse();
        }

        @Test
        @DisplayName("Should handle default status as PENDING")
        void shouldHandleDefaultStatusAsPending() {
            // Given
            UserProfileEntity entity = new UserProfileEntity();

            // Then
            assertThat(entity.getStatus()).isEqualTo(UserStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Complete Entity State")
    class CompleteEntityStateTests {

        @Test
        @DisplayName("Should handle complete entity with all fields set")
        void shouldHandleCompleteEntityWithAllFieldsSet() {
            // Given
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String email = "test@example.com";
            UserStatus status = UserStatus.ACTIVE;
            boolean mustChangePassword = true;
            boolean kycVerified = true;
            String kycDocumentUrl = "https://storage.example.com/kyc/doc123.pdf";
            Instant kycVerifiedAt = Instant.now();
            UUID approvedBy = UUID.randomUUID();
            Instant approvedAt = Instant.now();
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();
            String metadataJson = "{\"source\":\"web_registration\"}";

            // When
            userProfile.setId(id);
            userProfile.setUserId(userId);
            userProfile.setEmail(email);
            userProfile.setStatus(status);
            userProfile.setMustChangePassword(mustChangePassword);
            userProfile.setKycVerified(kycVerified);
            userProfile.setKycDocumentUrl(kycDocumentUrl);
            userProfile.setKycVerifiedAt(kycVerifiedAt);
            userProfile.setApprovedBy(approvedBy);
            userProfile.setApprovedAt(approvedAt);
            userProfile.setCreatedAt(createdAt);
            userProfile.setUpdatedAt(updatedAt);
            userProfile.setMetadataJson(metadataJson);

            // Then
            assertThat(userProfile.getId()).isEqualTo(id);
            assertThat(userProfile.getUserId()).isEqualTo(userId);
            assertThat(userProfile.getEmail()).isEqualTo(email);
            assertThat(userProfile.getStatus()).isEqualTo(status);
            assertThat(userProfile.isMustChangePassword()).isTrue();
            assertThat(userProfile.isKycVerified()).isTrue();
            assertThat(userProfile.getKycDocumentUrl()).isEqualTo(kycDocumentUrl);
            assertThat(userProfile.getKycVerifiedAt()).isEqualTo(kycVerifiedAt);
            assertThat(userProfile.getApprovedBy()).isEqualTo(approvedBy);
            assertThat(userProfile.getApprovedAt()).isEqualTo(approvedAt);
            assertThat(userProfile.getCreatedAt()).isEqualTo(createdAt);
            assertThat(userProfile.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(userProfile.getMetadataJson()).isEqualTo(metadataJson);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String email = "test@example.com";

            // When
            userProfile.setId(id);
            userProfile.setUserId(userId);
            userProfile.setEmail(email);

            // Then
            assertThat(userProfile.getId()).isEqualTo(id);
            assertThat(userProfile.getUserId()).isEqualTo(userId);
            assertThat(userProfile.getEmail()).isEqualTo(email);
            assertThat(userProfile.getStatus()).isEqualTo(UserStatus.PENDING);
            assertThat(userProfile.isKycVerified()).isFalse();
            assertThat(userProfile.isMustChangePassword()).isFalse();
        }

        @Test
        @DisplayName("Should handle entity with PENDING status")
        void shouldHandleEntityWithPendingStatus() {
            // Given
            userProfile.setId(UUID.randomUUID());
            userProfile.setUserId(UUID.randomUUID());
            userProfile.setEmail("test@example.com");
            userProfile.setStatus(UserStatus.PENDING);

            // Then
            assertThat(userProfile.getStatus()).isEqualTo(UserStatus.PENDING);
            assertThat(userProfile.isPending()).isTrue();
            assertThat(userProfile.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should handle entity with ACTIVE status and approved fields")
        void shouldHandleEntityWithActiveStatusAndApprovedFields() {
            // Given
            UUID approvedBy = UUID.randomUUID();
            Instant approvedAt = Instant.now();

            userProfile.setId(UUID.randomUUID());
            userProfile.setUserId(UUID.randomUUID());
            userProfile.setEmail("test@example.com");
            userProfile.setStatus(UserStatus.ACTIVE);
            userProfile.setApprovedBy(approvedBy);
            userProfile.setApprovedAt(approvedAt);

            // Then
            assertThat(userProfile.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(userProfile.isActive()).isTrue();
            assertThat(userProfile.getApprovedBy()).isEqualTo(approvedBy);
            assertThat(userProfile.getApprovedAt()).isEqualTo(approvedAt);
        }

        @Test
        @DisplayName("Should handle entity with KYC verified")
        void shouldHandleEntityWithKycVerified() {
            // Given
            Instant kycVerifiedAt = Instant.now();

            userProfile.setId(UUID.randomUUID());
            userProfile.setUserId(UUID.randomUUID());
            userProfile.setEmail("test@example.com");
            userProfile.setKycVerified(true);
            userProfile.setKycDocumentUrl("https://storage.example.com/kyc/doc.pdf");
            userProfile.setKycVerifiedAt(kycVerifiedAt);

            // Then
            assertThat(userProfile.isKycVerified()).isTrue();
            assertThat(userProfile.getKycDocumentUrl()).isNotNull();
            assertThat(userProfile.getKycVerifiedAt()).isEqualTo(kycVerifiedAt);
        }
    }
}
