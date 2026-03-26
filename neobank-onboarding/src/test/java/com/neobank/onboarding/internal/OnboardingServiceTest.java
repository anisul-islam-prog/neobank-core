package com.neobank.onboarding.internal;

import com.neobank.auth.UserRole;
import com.neobank.onboarding.ApprovalResult;
import com.neobank.onboarding.UserStatus;
import com.neobank.onboarding.api.UserAccountRequestedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OnboardingService using JUnit 5 and Mockito.
 * Tests user registration, approval, and status management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingService Unit Tests")
class OnboardingServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        onboardingService = new OnboardingService(userProfileRepository, eventPublisher);
    }

    @Nested
    @DisplayName("User Registration")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully with PENDING status")
        void shouldRegisterUserSuccessfullyWithPendingStatus() {
            // Given
            String username = "testuser";
            String email = "test@example.com";
            String password = "SecurePass123!";
            UUID userId = UUID.randomUUID();

            given(userProfileRepository.existsByEmail(email)).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(new UserProfileEntity());

            // When
            OnboardingService.OnboardingResult result = onboardingService.registerUser(username, email, password);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.userId()).isNotNull();
            assertThat(result.message()).contains("Registration successful");

            ArgumentCaptor<UserProfileEntity> profileCaptor = ArgumentCaptor.forClass(UserProfileEntity.class);
            verify(userProfileRepository).save(profileCaptor.capture());

            UserProfileEntity savedProfile = profileCaptor.getValue();
            assertThat(savedProfile.getEmail()).isEqualTo(email);
            assertThat(savedProfile.getStatus()).isEqualTo(UserStatus.PENDING);
            assertThat(savedProfile.isKycVerified()).isFalse();
            assertThat(savedProfile.isMustChangePassword()).isFalse();
        }

        @Test
        @DisplayName("Should publish UserAccountRequestedEvent after successful registration")
        void shouldPublishUserAccountRequestedEventAfterSuccessfulRegistration() {
            // Given
            String username = "testuser";
            String email = "test@example.com";
            String password = "SecurePass123!";

            given(userProfileRepository.existsByEmail(email)).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(new UserProfileEntity());

            // When
            onboardingService.registerUser(username, email, password);

            // Then
            ArgumentCaptor<UserAccountRequestedEvent> eventCaptor = ArgumentCaptor.forClass(UserAccountRequestedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            UserAccountRequestedEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.username()).isEqualTo(username);
            assertThat(publishedEvent.email()).isEqualTo(email);
            assertThat(publishedEvent.userId()).isNotNull();
        }

        @Test
        @DisplayName("Should fail registration when email already exists")
        void shouldFailRegistrationWhenEmailAlreadyExists() {
            // Given
            String username = "testuser";
            String email = "existing@example.com";
            String password = "SecurePass123!";

            given(userProfileRepository.existsByEmail(email)).willReturn(true);

            // When
            OnboardingService.OnboardingResult result = onboardingService.registerUser(username, email, password);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Email already registered");
            verify(userProfileRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should handle null username")
        void shouldHandleNullUsername() {
            // Given
            String email = "test@example.com";
            String password = "SecurePass123!";

            given(userProfileRepository.existsByEmail(email)).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(new UserProfileEntity());

            // When
            OnboardingService.OnboardingResult result = onboardingService.registerUser(null, email, password);

            // Then
            assertThat(result.success()).isTrue();
            verify(eventPublisher).publishEvent(any(UserAccountRequestedEvent.class));
        }

        @Test
        @DisplayName("Should handle null password")
        void shouldHandleNullPassword() {
            // Given
            String username = "testuser";
            String email = "test@example.com";

            given(userProfileRepository.existsByEmail(email)).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(new UserProfileEntity());

            // When
            OnboardingService.OnboardingResult result = onboardingService.registerUser(username, email, null);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should handle empty password")
        void shouldHandleEmptyPassword() {
            // Given
            String username = "testuser";
            String email = "test@example.com";
            String password = "";

            given(userProfileRepository.existsByEmail(email)).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(new UserProfileEntity());

            // When
            OnboardingService.OnboardingResult result = onboardingService.registerUser(username, email, password);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() {
            // Given
            String username = "testuser";
            String email = "test+user@example.com";
            String password = "SecurePass123!";

            given(userProfileRepository.existsByEmail(email)).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(new UserProfileEntity());

            // When
            OnboardingService.OnboardingResult result = onboardingService.registerUser(username, email, password);

            // Then
            assertThat(result.success()).isTrue();

            ArgumentCaptor<UserProfileEntity> profileCaptor = ArgumentCaptor.forClass(UserProfileEntity.class);
            verify(userProfileRepository).save(profileCaptor.capture());
            assertThat(profileCaptor.getValue().getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should handle email with unicode characters")
        void shouldHandleEmailWithUnicodeCharacters() {
            // Given
            String username = "testuser";
            String email = "用户@example.com";
            String password = "SecurePass123!";

            given(userProfileRepository.existsByEmail(email)).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(new UserProfileEntity());

            // When
            OnboardingService.OnboardingResult result = onboardingService.registerUser(username, email, password);

            // Then
            assertThat(result.success()).isTrue();
        }
    }

    @Nested
    @DisplayName("User Approval")
    class UserApprovalTests {

        @Test
        @DisplayName("Should approve pending user by MANAGER")
        void shouldApprovePendingUserByManager() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.MANAGER;

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setEmail("test@example.com");
            profile.setStatus(UserStatus.PENDING);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(userProfileRepository.save(profile)).willReturn(profile);

            // When
            ApprovalResult result = onboardingService.approveUser(userId, approverId, approverRole);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(profile.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(profile.getApprovedBy()).isEqualTo(approverId);
            assertThat(profile.getApprovedAt()).isNotNull();
            verify(userProfileRepository).save(profile);
        }

        @Test
        @DisplayName("Should approve pending user by RELATIONSHIP_OFFICER")
        void shouldApprovePendingUserByRelationshipOfficer() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.RELATIONSHIP_OFFICER;

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.PENDING);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(userProfileRepository.save(profile)).willReturn(profile);

            // When
            ApprovalResult result = onboardingService.approveUser(userId, approverId, approverRole);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should approve pending user by SYSTEM_ADMIN")
        void shouldApprovePendingUserBySystemAdmin() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.SYSTEM_ADMIN;

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.PENDING);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(userProfileRepository.save(profile)).willReturn(profile);

            // When
            ApprovalResult result = onboardingService.approveUser(userId, approverId, approverRole);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should reject approval by CUSTOMER_RETAIL")
        void shouldRejectApprovalByCustomerRetail() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.CUSTOMER_RETAIL;

            // When
            ApprovalResult result = onboardingService.approveUser(userId, approverId, approverRole);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
            verify(userProfileRepository, never()).findByUserId(userId);
        }

        @Test
        @DisplayName("Should reject approval by TELLER")
        void shouldRejectApprovalByTeller() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.TELLER;

            // When
            ApprovalResult result = onboardingService.approveUser(userId, approverId, approverRole);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
        }

        @Test
        @DisplayName("Should reject approval by AUDITOR")
        void shouldRejectApprovalByAuditor() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.AUDITOR;

            // When
            ApprovalResult result = onboardingService.approveUser(userId, approverId, approverRole);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
        }

        @Test
        @DisplayName("Should reject approval when user profile not found")
        void shouldRejectApprovalWhenUserProfileNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.MANAGER;

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.empty());

            // When
            ApprovalResult result = onboardingService.approveUser(userId, approverId, approverRole);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("User profile not found");
        }

        @Test
        @DisplayName("Should reject approval when user is not in PENDING status")
        void shouldRejectApprovalWhenUserIsNotInPendingStatus() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.MANAGER;

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.ACTIVE);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            // When
            ApprovalResult result = onboardingService.approveUser(userId, approverId, approverRole);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("not in PENDING status");
        }

        @Test
        @DisplayName("Should reject approval when user is SUSPENDED")
        void shouldRejectApprovalWhenUserIsSuspended() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.MANAGER;

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.SUSPENDED);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            // When
            ApprovalResult result = onboardingService.approveUser(userId, approverId, approverRole);

            // Then
            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should set updatedAt timestamp on approval")
        void shouldSetUpdatedAtTimestampOnApproval() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.MANAGER;
            Instant beforeApproval = Instant.now();

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.PENDING);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(userProfileRepository.save(profile)).willReturn(profile);

            // When
            onboardingService.approveUser(userId, approverId, approverRole);

            // Then
            assertThat(profile.getUpdatedAt()).isNotNull();
            assertThat(profile.getUpdatedAt()).isAfterOrEqualTo(beforeApproval);
        }
    }

    @Nested
    @DisplayName("User Status Update")
    class UserStatusUpdateTests {

        @Test
        @DisplayName("Should update user status to SUSPENDED by SYSTEM_ADMIN")
        void shouldUpdateUserStatusToSuspendedBySystemAdmin() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID updaterId = UUID.randomUUID();
            UserRole updaterRole = UserRole.SYSTEM_ADMIN;
            UserStatus newStatus = UserStatus.SUSPENDED;

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.ACTIVE);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(userProfileRepository.save(profile)).willReturn(profile);

            // When
            ApprovalResult result = onboardingService.updateUserStatus(userId, newStatus, updaterRole);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(profile.getStatus()).isEqualTo(UserStatus.SUSPENDED);
            verify(userProfileRepository).save(profile);
        }

        @Test
        @DisplayName("Should update user status to ACTIVE by MANAGER")
        void shouldUpdateUserStatusToActiveByManager() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID updaterId = UUID.randomUUID();
            UserRole updaterRole = UserRole.MANAGER;
            UserStatus newStatus = UserStatus.ACTIVE;

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.SUSPENDED);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(userProfileRepository.save(profile)).willReturn(profile);

            // When
            ApprovalResult result = onboardingService.updateUserStatus(userId, newStatus, updaterRole);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(profile.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should reject status update by CUSTOMER_RETAIL")
        void shouldRejectStatusUpdateByCustomerRetail() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID updaterId = UUID.randomUUID();
            UserRole updaterRole = UserRole.CUSTOMER_RETAIL;
            UserStatus newStatus = UserStatus.SUSPENDED;

            // When
            ApprovalResult result = onboardingService.updateUserStatus(userId, newStatus, updaterRole);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
        }

        @Test
        @DisplayName("Should reject status update by TELLER")
        void shouldRejectStatusUpdateByTeller() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID updaterId = UUID.randomUUID();
            UserRole updaterRole = UserRole.TELLER;
            UserStatus newStatus = UserStatus.SUSPENDED;

            // When
            ApprovalResult result = onboardingService.updateUserStatus(userId, newStatus, updaterRole);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
        }

        @Test
        @DisplayName("Should reject status update by RELATIONSHIP_OFFICER")
        void shouldRejectStatusUpdateByRelationshipOfficer() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID updaterId = UUID.randomUUID();
            UserRole updaterRole = UserRole.RELATIONSHIP_OFFICER;
            UserStatus newStatus = UserStatus.SUSPENDED;

            // When
            ApprovalResult result = onboardingService.updateUserStatus(userId, newStatus, updaterRole);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
        }

        @Test
        @DisplayName("Should reject status update when user profile not found")
        void shouldRejectStatusUpdateWhenUserProfileNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID updaterId = UUID.randomUUID();
            UserRole updaterRole = UserRole.SYSTEM_ADMIN;
            UserStatus newStatus = UserStatus.SUSPENDED;

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.empty());

            // When
            ApprovalResult result = onboardingService.updateUserStatus(userId, newStatus, updaterRole);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("User profile not found");
        }

        @Test
        @DisplayName("Should set updatedAt timestamp on status update")
        void shouldSetUpdatedAtTimestampOnStatusUpdate() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID updaterId = UUID.randomUUID();
            UserRole updaterRole = UserRole.SYSTEM_ADMIN;
            UserStatus newStatus = UserStatus.SUSPENDED;
            Instant beforeUpdate = Instant.now();

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.ACTIVE);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(userProfileRepository.save(profile)).willReturn(profile);

            // When
            onboardingService.updateUserStatus(userId, newStatus, updaterRole);

            // Then
            assertThat(profile.getUpdatedAt()).isNotNull();
            assertThat(profile.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }
    }

    @Nested
    @DisplayName("User Profile Retrieval")
    class UserProfileRetrievalTests {

        @Test
        @DisplayName("Should get user profile by user ID")
        void shouldGetUserProfileByUserId() {
            // Given
            UUID userId = UUID.randomUUID();
            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setEmail("test@example.com");
            profile.setStatus(UserStatus.ACTIVE);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            // When
            Optional<UserProfileEntity> result = onboardingService.getUserProfile(userId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return empty when user profile not found")
        void shouldReturnEmptyWhenUserProfileNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.empty());

            // When
            Optional<UserProfileEntity> result = onboardingService.getUserProfile(userId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get user status by user ID")
        void shouldGetUserStatusByUserId() {
            // Given
            UUID userId = UUID.randomUUID();
            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.ACTIVE);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            // When
            UserStatus status = onboardingService.getUserStatus(userId);

            // Then
            assertThat(status).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should return null when user status not found")
        void shouldReturnNullWhenUserStatusNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.empty());

            // When
            UserStatus status = onboardingService.getUserStatus(userId);

            // Then
            assertThat(status).isNull();
        }

        @Test
        @DisplayName("Should check if user is active")
        void shouldCheckIfUserIsActive() {
            // Given
            UUID userId = UUID.randomUUID();
            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.ACTIVE);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            // When
            boolean isActive = onboardingService.isActiveUser(userId);

            // Then
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("Should return false when user is not active")
        void shouldReturnFalseWhenUserIsNotActive() {
            // Given
            UUID userId = UUID.randomUUID();
            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.PENDING);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

            // When
            boolean isActive = onboardingService.isActiveUser(userId);

            // Then
            assertThat(isActive).isFalse();
        }

        @Test
        @DisplayName("Should return false when user profile not found for isActive check")
        void shouldReturnFalseWhenUserProfileNotFoundForIsActiveCheck() {
            // Given
            UUID userId = UUID.randomUUID();
            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.empty());

            // When
            boolean isActive = onboardingService.isActiveUser(userId);

            // Then
            assertThat(isActive).isFalse();
        }
    }

    @Nested
    @DisplayName("OnboardingResult Factory Methods")
    class OnboardingResultFactoryMethodsTests {

        @Test
        @DisplayName("Should create success result")
        void shouldCreateSuccessResult() {
            // Given
            UUID userId = UUID.randomUUID();
            String message = "Registration successful";

            // When
            OnboardingService.OnboardingResult result = OnboardingService.OnboardingResult.success(userId, message);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.message()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should create failure result")
        void shouldCreateFailureResult() {
            // Given
            String reason = "Email already registered";

            // When
            OnboardingService.OnboardingResult result = OnboardingService.OnboardingResult.failure(reason);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.userId()).isNull();
            assertThat(result.message()).isEqualTo(reason);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null approver ID")
        void shouldHandleNullApproverId() {
            // Given
            UUID userId = UUID.randomUUID();
            UserRole approverRole = UserRole.MANAGER;

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.PENDING);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(userProfileRepository.save(profile)).willReturn(profile);

            // When
            ApprovalResult result = onboardingService.approveUser(userId, null, approverRole);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(profile.getApprovedBy()).isNull();
        }

        @Test
        @DisplayName("Should handle null updater ID")
        void shouldHandleNullUpdaterId() {
            // Given
            UUID userId = UUID.randomUUID();
            UserRole updaterRole = UserRole.SYSTEM_ADMIN;
            UserStatus newStatus = UserStatus.SUSPENDED;

            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setStatus(UserStatus.ACTIVE);

            given(userProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
            given(userProfileRepository.save(profile)).willReturn(profile);

            // When
            ApprovalResult result = onboardingService.updateUserStatus(userId, newStatus, updaterRole);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should handle email with very long string")
        void shouldHandleEmailWithVeryLongString() {
            // Given
            String username = "testuser";
            String email = "a".repeat(200) + "@example.com";
            String password = "SecurePass123!";

            given(userProfileRepository.existsByEmail(email)).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(new UserProfileEntity());

            // When
            OnboardingService.OnboardingResult result = onboardingService.registerUser(username, email, password);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should handle username with special characters")
        void shouldHandleUsernameWithSpecialCharacters() {
            // Given
            String username = "John O'Brien-Smith";
            String email = "test@example.com";
            String password = "SecurePass123!";

            given(userProfileRepository.existsByEmail(email)).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(new UserProfileEntity());

            // When
            OnboardingService.OnboardingResult result = onboardingService.registerUser(username, email, password);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should handle username with unicode characters")
        void shouldHandleUsernameWithUnicodeCharacters() {
            // Given
            String username = "用户 测试";
            String email = "test@example.com";
            String password = "SecurePass123!";

            given(userProfileRepository.existsByEmail(email)).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(new UserProfileEntity());

            // When
            OnboardingService.OnboardingResult result = onboardingService.registerUser(username, email, password);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should handle multiple registrations with same email")
        void shouldHandleMultipleRegistrationsWithSameEmail() {
            // Given
            String username = "testuser";
            String email = "duplicate@example.com";
            String password = "SecurePass123!";

            given(userProfileRepository.existsByEmail(email)).willReturn(true);

            // When
            OnboardingService.OnboardingResult result1 = onboardingService.registerUser(username, email, password);
            OnboardingService.OnboardingResult result2 = onboardingService.registerUser(username + "2", email, password);

            // Then
            assertThat(result1.success()).isFalse();
            assertThat(result2.success()).isFalse();
        }

        @Test
        @DisplayName("Should handle multiple approvals in sequence")
        void shouldHandleMultipleApprovalsInSequence() {
            // Given
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            UserRole approverRole = UserRole.MANAGER;

            UserProfileEntity profile1 = new UserProfileEntity();
            profile1.setId(UUID.randomUUID());
            profile1.setUserId(userId1);
            profile1.setStatus(UserStatus.PENDING);

            UserProfileEntity profile2 = new UserProfileEntity();
            profile2.setId(UUID.randomUUID());
            profile2.setUserId(userId2);
            profile2.setStatus(UserStatus.PENDING);

            given(userProfileRepository.findByUserId(userId1)).willReturn(Optional.of(profile1));
            given(userProfileRepository.findByUserId(userId2)).willReturn(Optional.of(profile2));
            given(userProfileRepository.save(profile1)).willReturn(profile1);
            given(userProfileRepository.save(profile2)).willReturn(profile2);

            // When
            ApprovalResult result1 = onboardingService.approveUser(userId1, approverId, approverRole);
            ApprovalResult result2 = onboardingService.approveUser(userId2, approverId, approverRole);

            // Then
            assertThat(result1.success()).isTrue();
            assertThat(result2.success()).isTrue();
            assertThat(profile1.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(profile2.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }
}
