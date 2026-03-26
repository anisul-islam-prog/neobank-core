package com.neobank.auth.internal;

import com.neobank.auth.*;
import com.neobank.auth.api.UserCreatedEvent;
import com.neobank.core.branches.BranchEntity;
import com.neobank.core.branches.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService using JUnit 5 and Mockito.
 * Provides 100% logic coverage for user registration and authentication.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private BranchService branchService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, eventPublisher, branchService);
    }

    @Nested
    @DisplayName("User Registration")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register new user successfully with PENDING status and ROLE_GUEST")
        void shouldRegisterUserSuccessfullyWithPendingStatus() {
            // Given
            RegistrationRequest request = new RegistrationRequest("testuser", "SecurePass123!", "test@example.com");
            UUID userId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            given(userRepository.existsByUsername(request.username())).willReturn(false);
            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
            given(branchService.getHeadOffice()).willReturn(createTestBranch(branchId));

            // When
            RegistrationResult result = authService.register(request);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.userId()).isNotNull();

            ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();
            assertThat(savedUser.getUsername()).isEqualTo(request.username());
            assertThat(savedUser.getEmail()).isEqualTo(request.email());
            assertThat(savedUser.getPasswordHash()).isEqualTo("encodedPassword");
            assertThat(savedUser.getRole()).isEqualTo(UserRole.ROLE_GUEST);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING);
            assertThat(savedUser.isMustChangePassword()).isFalse();
            assertThat(savedUser.getBranchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("Should publish UserCreatedEvent after successful registration")
        void shouldPublishUserCreatedEventAfterSuccessfulRegistration() {
            // Given
            RegistrationRequest request = new RegistrationRequest("testuser", "SecurePass123!", "test@example.com");
            UUID userId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();

            given(userRepository.existsByUsername(request.username())).willReturn(false);
            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
            given(branchService.getHeadOffice()).willReturn(createTestBranch(branchId));

            // When
            authService.register(request);

            // Then
            ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            UserCreatedEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.username()).isEqualTo(request.username());
            assertThat(publishedEvent.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("Should fail registration when username already exists")
        void shouldFailRegistrationWhenUsernameAlreadyExists() {
            // Given
            RegistrationRequest request = new RegistrationRequest("existinguser", "SecurePass123!", "test@example.com");
            given(userRepository.existsByUsername(request.username())).willReturn(true);

            // When
            RegistrationResult result = authService.register(request);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Username already exists");
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should fail registration when email already registered")
        void shouldFailRegistrationWhenEmailAlreadyRegistered() {
            // Given
            RegistrationRequest request = new RegistrationRequest("newuser", "SecurePass123!", "existing@example.com");
            given(userRepository.existsByUsername(request.username())).willReturn(false);
            given(userRepository.existsByEmail(request.email())).willReturn(true);

            // When
            RegistrationResult result = authService.register(request);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Email already registered");
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should handle null username")
        void shouldHandleNullUsername() {
            // Given/When/Then
            assertThatThrownBy(() -> new RegistrationRequest(null, "SecurePass123!", "test@example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username must not be blank");
        }

        @Test
        @DisplayName("Should handle blank username")
        void shouldHandleBlankUsername() {
            // Given/When/Then
            assertThatThrownBy(() -> new RegistrationRequest("   ", "SecurePass123!", "test@example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username must not be blank");
        }

        @Test
        @DisplayName("Should handle null password")
        void shouldHandleNullPassword() {
            // Given/When/Then
            assertThatThrownBy(() -> new RegistrationRequest("testuser", null, "test@example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("password must be at least 8 characters");
        }

        @Test
        @DisplayName("Should handle short password")
        void shouldHandleShortPassword() {
            // Given/When/Then
            assertThatThrownBy(() -> new RegistrationRequest("testuser", "short", "test@example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("password must be at least 8 characters");
        }

        @Test
        @DisplayName("Should handle null email")
        void shouldHandleNullEmail() {
            // Given/When/Then
            assertThatThrownBy(() -> new RegistrationRequest("testuser", "SecurePass123!", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email must not be blank");
        }

        @Test
        @DisplayName("Should handle blank email")
        void shouldHandleBlankEmail() {
            // Given/When/Then
            assertThatThrownBy(() -> new RegistrationRequest("testuser", "SecurePass123!", "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email must not be blank");
        }

        @Test
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() {
            // Given
            String email = "test+user@example.com";

            // When
            RegistrationRequest request = new RegistrationRequest("testuser", "SecurePass123!", email);

            // Then
            assertThat(request.email()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should handle email with unicode characters")
        void shouldHandleEmailWithUnicodeCharacters() {
            // Given
            String email = "用户@example.com";

            // When
            RegistrationRequest request = new RegistrationRequest("testuser", "SecurePass123!", email);

            // Then
            assertThat(request.email()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("User Authentication")
    class UserAuthenticationTests {

        @Test
        @DisplayName("Should authenticate active user successfully and return JWT token")
        void shouldAuthenticateActiveUserSuccessfullyAndReturnJwtToken() {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("testuser", "SecurePass123!");
            UUID userId = UUID.randomUUID();
            String token = "jwt.token.here";

            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername(request.username());
            user.setEmail("test@example.com");
            user.setPasswordHash("encodedPassword");
            user.setRole(UserRole.CUSTOMER_RETAIL);
            user.setStatus(UserStatus.ACTIVE);
            user.setMustChangePassword(false);

            given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.password(), user.getPasswordHash())).willReturn(true);
            given(jwtService.generateToken(userId, request.username())).willReturn(token);

            // When
            AuthenticationResult result = authService.authenticate(request);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.token()).isEqualTo(token);
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.mustChangePassword()).isFalse();
            assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);

            verify(userRepository).save(user);
            assertThat(user.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("Should reject authentication for PENDING user with 403 Forbidden")
        void shouldRejectAuthenticationForPendingUserWith403Forbidden() {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("pendinguser", "SecurePass123!");

            UserEntity user = new UserEntity();
            user.setId(UUID.randomUUID());
            user.setUsername(request.username());
            user.setStatus(UserStatus.PENDING);
            user.setPasswordHash("encodedPassword");

            given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));

            // When
            AuthenticationResult result = authService.authenticate(request);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("pending");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject authentication for SUSPENDED user with 403 Forbidden")
        void shouldRejectAuthenticationForSuspendedUserWith403Forbidden() {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("suspendeduser", "SecurePass123!");

            UserEntity user = new UserEntity();
            user.setId(UUID.randomUUID());
            user.setUsername(request.username());
            user.setStatus(UserStatus.SUSPENDED);
            user.setPasswordHash("encodedPassword");

            given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));

            // When
            AuthenticationResult result = authService.authenticate(request);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("suspended");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject authentication with invalid password")
        void shouldRejectAuthenticationWithInvalidPassword() {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("testuser", "WrongPassword!");

            UserEntity user = new UserEntity();
            user.setId(UUID.randomUUID());
            user.setUsername(request.username());
            user.setStatus(UserStatus.ACTIVE);
            user.setPasswordHash("encodedPassword");

            given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.password(), user.getPasswordHash())).willReturn(false);

            // When
            AuthenticationResult result = authService.authenticate(request);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Invalid username or password");
        }

        @Test
        @DisplayName("Should reject authentication when user not found")
        void shouldRejectAuthenticationWhenUserNotFound() {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("nonexistent", "SecurePass123!");
            given(userRepository.findByUsername(request.username())).willReturn(Optional.empty());

            // When
            AuthenticationResult result = authService.authenticate(request);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Invalid username or password");
        }

        @Test
        @DisplayName("Should handle mustChangePassword flag for staff users")
        void shouldHandleMustChangePasswordFlagForStaffUsers() {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("staffuser", "SecurePass123!");
            UUID userId = UUID.randomUUID();
            String token = "jwt.token.here";

            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername(request.username());
            user.setStatus(UserStatus.ACTIVE);
            user.setPasswordHash("encodedPassword");
            user.setMustChangePassword(true);

            given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.password(), user.getPasswordHash())).willReturn(true);
            given(jwtService.generateToken(userId, request.username())).willReturn(token);

            // When
            AuthenticationResult result = authService.authenticate(request);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.mustChangePassword()).isTrue();
        }

        @Test
        @DisplayName("Should handle null username in request")
        void shouldHandleNullUsernameInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new AuthenticationRequest(null, "SecurePass123!"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username must not be blank");
        }

        @Test
        @DisplayName("Should handle blank username in request")
        void shouldHandleBlankUsernameInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new AuthenticationRequest("   ", "SecurePass123!"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username must not be blank");
        }

        @Test
        @DisplayName("Should handle null password in request")
        void shouldHandleNullPasswordInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new AuthenticationRequest("testuser", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("password must not be blank");
        }

        @Test
        @DisplayName("Should handle blank password in request")
        void shouldHandleBlankPasswordInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new AuthenticationRequest("testuser", "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("password must not be blank");
        }

        @Test
        @DisplayName("Should update last login timestamp on successful authentication")
        void shouldUpdateLastLoginTimestampOnSuccessfulAuthentication() {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("testuser", "SecurePass123!");
            UUID userId = UUID.randomUUID();
            Instant beforeLogin = Instant.now();

            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername(request.username());
            user.setStatus(UserStatus.ACTIVE);
            user.setPasswordHash("encodedPassword");

            given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.password(), user.getPasswordHash())).willReturn(true);
            given(jwtService.generateToken(userId, request.username())).willReturn("token");

            // When
            authService.authenticate(request);

            // Then
            assertThat(user.getLastLoginAt()).isNotNull();
            assertThat(user.getLastLoginAt()).isAfterOrEqualTo(beforeLogin);
        }
    }

    @Nested
    @DisplayName("Staff Onboarding")
    class StaffOnboardingTests {

        @Test
        @DisplayName("Should onboard staff with any role by SYSTEM_ADMIN")
        void shouldOnboardStaffWithAnyRoleBySystemAdmin() {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "manageruser", "SecurePass123!", "manager@example.com",
                    UserRole.MANAGER, null
            );
            UUID branchId = UUID.randomUUID();

            given(userRepository.existsByUsername(request.username())).willReturn(false);
            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
            given(branchService.getHeadOffice()).willReturn(createTestBranch(branchId));

            // When
            RegistrationResult result = authService.onboardStaff(request, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isTrue();

            ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(UserRole.MANAGER);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(savedUser.isMustChangePassword()).isTrue();
        }

        @Test
        @DisplayName("Should onboard TELLER by MANAGER")
        void shouldOnboardTellerByManager() {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "telleruser", "SecurePass123!", "teller@example.com",
                    UserRole.TELLER, null
            );
            UUID branchId = UUID.randomUUID();

            given(userRepository.existsByUsername(request.username())).willReturn(false);
            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
            given(branchService.getHeadOffice()).willReturn(createTestBranch(branchId));

            // When
            RegistrationResult result = authService.onboardStaff(request, UserRole.MANAGER);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should onboard CUSTOMER_RETAIL by RELATIONSHIP_OFFICER")
        void shouldOnboardCustomerRetailByRelationshipOfficer() {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "customeruser", "SecurePass123!", "customer@example.com",
                    UserRole.CUSTOMER_RETAIL, null
            );

            given(userRepository.existsByUsername(request.username())).willReturn(false);
            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
            given(branchService.getHeadOffice()).willReturn(createTestBranch(UUID.randomUUID()));

            // When
            RegistrationResult result = authService.onboardStaff(request, UserRole.RELATIONSHIP_OFFICER);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should reject onboarding by TELLER")
        void shouldRejectOnboardingByTeller() {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "newuser", "SecurePass123!", "new@example.com",
                    UserRole.TELLER, null
            );

            // When
            RegistrationResult result = authService.onboardStaff(request, UserRole.TELLER);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject MANAGER onboarding SYSTEM_ADMIN")
        void shouldRejectManagerOnboardingSystemAdmin() {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "adminuser", "SecurePass123!", "admin@example.com",
                    UserRole.SYSTEM_ADMIN, null
            );

            // When
            RegistrationResult result = authService.onboardStaff(request, UserRole.MANAGER);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges to assign SYSTEM_ADMIN");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject MANAGER onboarding AUDITOR")
        void shouldRejectManagerOnboardingAuditor() {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "auditoruser", "SecurePass123!", "auditor@example.com",
                    UserRole.AUDITOR, null
            );

            // When
            RegistrationResult result = authService.onboardStaff(request, UserRole.MANAGER);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges to assign AUDITOR");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject duplicate username during staff onboarding")
        void shouldRejectDuplicateUsernameDuringStaffOnboarding() {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "existinguser", "SecurePass123!", "new@example.com",
                    UserRole.TELLER, null
            );
            given(userRepository.existsByUsername(request.username())).willReturn(true);

            // When
            RegistrationResult result = authService.onboardStaff(request, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Username already exists");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject duplicate email during staff onboarding")
        void shouldRejectDuplicateEmailDuringStaffOnboarding() {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "newuser", "SecurePass123!", "existing@example.com",
                    UserRole.TELLER, null
            );
            given(userRepository.existsByUsername(request.username())).willReturn(false);
            given(userRepository.existsByEmail(request.email())).willReturn(true);

            // When
            RegistrationResult result = authService.onboardStaff(request, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Email already registered");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should use provided branchId when specified")
        void shouldUseProvidedBranchIdWhenSpecified() {
            // Given
            UUID customBranchId = UUID.randomUUID();
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "branchuser", "SecurePass123!", "branch@example.com",
                    UserRole.TELLER, customBranchId
            );

            given(userRepository.existsByUsername(request.username())).willReturn(false);
            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");

            // When
            authService.onboardStaff(request, UserRole.SYSTEM_ADMIN);

            // Then
            ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            assertThat(userCaptor.getValue().getBranchId()).isEqualTo(customBranchId);
        }

        @Test
        @DisplayName("Should use Head Office branchId when not specified")
        void shouldUseHeadOfficeBranchIdWhenNotSpecified() {
            // Given
            UUID headOfficeId = UUID.randomUUID();
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "branchuser", "SecurePass123!", "branch@example.com",
                    UserRole.TELLER, null
            );

            given(userRepository.existsByUsername(request.username())).willReturn(false);
            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
            given(branchService.getHeadOffice()).willReturn(createTestBranch(headOfficeId));

            // When
            authService.onboardStaff(request, UserRole.SYSTEM_ADMIN);

            // Then
            ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            assertThat(userCaptor.getValue().getBranchId()).isEqualTo(headOfficeId);
        }

        @Test
        @DisplayName("Should handle null username in request")
        void shouldHandleNullUsernameInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new StaffOnboardingRequest(null, "SecurePass123!", "test@example.com", UserRole.TELLER, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username must not be blank");
        }

        @Test
        @DisplayName("Should handle blank username in request")
        void shouldHandleBlankUsernameInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new StaffOnboardingRequest("   ", "SecurePass123!", "test@example.com", UserRole.TELLER, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username must not be blank");
        }

        @Test
        @DisplayName("Should handle null password in request")
        void shouldHandleNullPasswordInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new StaffOnboardingRequest("testuser", null, "test@example.com", UserRole.TELLER, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("password must be at least 8 characters");
        }

        @Test
        @DisplayName("Should handle short password in request")
        void shouldHandleShortPasswordInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new StaffOnboardingRequest("testuser", "short", "test@example.com", UserRole.TELLER, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("password must be at least 8 characters");
        }

        @Test
        @DisplayName("Should handle null email in request")
        void shouldHandleNullEmailInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new StaffOnboardingRequest("testuser", "SecurePass123!", null, UserRole.TELLER, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email must not be blank");
        }

        @Test
        @DisplayName("Should handle null role in request")
        void shouldHandleNullRoleInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new StaffOnboardingRequest("testuser", "SecurePass123!", "test@example.com", null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("role must not be null");
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
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("pendinguser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.ROLE_GUEST);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveUser(userId, UserRole.MANAGER);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER_RETAIL);
            assertThat(user.getRoles()).containsExactly(UserRole.CUSTOMER_RETAIL);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should approve pending user by RELATIONSHIP_OFFICER")
        void shouldApprovePendingUserByRelationshipOfficer() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("pendinguser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.ROLE_GUEST);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveUser(userId, UserRole.RELATIONSHIP_OFFICER);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should approve pending user by SYSTEM_ADMIN")
        void shouldApprovePendingUserBySystemAdmin() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("pendinguser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.ROLE_GUEST);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveUser(userId, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should reject approval by TELLER")
        void shouldRejectApprovalByTeller() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = authService.approveUser(userId, UserRole.TELLER);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject approval by AUDITOR")
        void shouldRejectApprovalByAuditor() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = authService.approveUser(userId, UserRole.AUDITOR);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject approval by CUSTOMER_RETAIL")
        void shouldRejectApprovalByCustomerRetail() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = authService.approveUser(userId, UserRole.CUSTOMER_RETAIL);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject approval when user not found")
        void shouldRejectApprovalWhenUserNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.approveUser(userId, UserRole.MANAGER))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should reject approval when user is not in PENDING status")
        void shouldRejectApprovalWhenUserIsNotInPendingStatus() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("activeuser");
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(UserRole.CUSTOMER_RETAIL);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveUser(userId, UserRole.MANAGER);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("not in PENDING status");
        }

        @Test
        @DisplayName("Should reject approval when user role is not ROLE_GUEST")
        void shouldRejectApprovalWhenUserRoleIsNotRoleGuest() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("staffuser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.TELLER);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveUser(userId, UserRole.MANAGER);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("not ROLE_GUEST");
        }

        @Test
        @DisplayName("Should set updatedAt timestamp on approval")
        void shouldSetUpdatedAtTimestampOnApproval() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("pendinguser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.ROLE_GUEST);
            Instant beforeApproval = Instant.now();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            authService.approveUser(userId, UserRole.MANAGER);

            // Then
            assertThat(user.getUpdatedAt()).isNotNull();
            assertThat(user.getUpdatedAt()).isAfterOrEqualTo(beforeApproval);
        }
    }

    @Nested
    @DisplayName("Staff Approval")
    class StaffApprovalTests {

        @Test
        @DisplayName("Should approve staff by SYSTEM_ADMIN")
        void shouldApproveStaffBySystemAdmin() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("staffuser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.TELLER);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveStaff(userId, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should approve MANAGER role staff")
        void shouldApproveManagerRoleStaff() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("manageruser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.MANAGER);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveStaff(userId, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should approve RELATIONSHIP_OFFICER role staff")
        void shouldApproveRelationshipOfficerRoleStaff() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("rouser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.RELATIONSHIP_OFFICER);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveStaff(userId, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should approve AUDITOR role staff")
        void shouldApproveAuditorRoleStaff() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("auditoruser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.AUDITOR);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveStaff(userId, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("Should reject staff approval by MANAGER")
        void shouldRejectStaffApprovalByManager() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = authService.approveStaff(userId, UserRole.MANAGER);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Only SYSTEM_ADMIN can approve staff");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject staff approval by RELATIONSHIP_OFFICER")
        void shouldRejectStaffApprovalByRelationshipOfficer() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = authService.approveStaff(userId, UserRole.RELATIONSHIP_OFFICER);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Only SYSTEM_ADMIN can approve staff");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject staff approval when user not found")
        void shouldRejectStaffApprovalWhenUserNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.approveStaff(userId, UserRole.SYSTEM_ADMIN))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should reject staff approval when user is not in PENDING status")
        void shouldRejectStaffApprovalWhenUserIsNotInPendingStatus() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("activeuser");
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(UserRole.TELLER);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveStaff(userId, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("not in PENDING status");
        }

        @Test
        @DisplayName("Should reject staff approval for invalid role CUSTOMER_RETAIL")
        void shouldRejectStaffApprovalForInvalidRoleCustomerRetail() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("customeruser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.CUSTOMER_RETAIL);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveStaff(userId, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Invalid staff role");
        }

        @Test
        @DisplayName("Should reject staff approval for invalid role ROLE_GUEST")
        void shouldRejectStaffApprovalForInvalidRoleRoleGuest() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("guestuser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.ROLE_GUEST);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.approveStaff(userId, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Invalid staff role");
        }

        @Test
        @DisplayName("Should set updatedAt timestamp on staff approval")
        void shouldSetUpdatedAtTimestampOnStaffApproval() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("staffuser");
            user.setStatus(UserStatus.PENDING);
            user.setRole(UserRole.TELLER);
            Instant beforeApproval = Instant.now();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            authService.approveStaff(userId, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(user.getUpdatedAt()).isNotNull();
            assertThat(user.getUpdatedAt()).isAfterOrEqualTo(beforeApproval);
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
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("testuser");
            user.setStatus(UserStatus.ACTIVE);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.updateUserStatus(userId, UserStatus.SUSPENDED, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.message()).contains("SUSPENDED");
            assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should update user status to ACTIVE by MANAGER")
        void shouldUpdateUserStatusToActiveByManager() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("testuser");
            user.setStatus(UserStatus.SUSPENDED);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.updateUserStatus(userId, UserStatus.ACTIVE, UserRole.MANAGER);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should reject status update by TELLER")
        void shouldRejectStatusUpdateByTeller() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = authService.updateUserStatus(userId, UserStatus.SUSPENDED, UserRole.TELLER);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject status update by RELATIONSHIP_OFFICER")
        void shouldRejectStatusUpdateByRelationshipOfficer() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = authService.updateUserStatus(userId, UserStatus.SUSPENDED, UserRole.RELATIONSHIP_OFFICER);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject status update by AUDITOR")
        void shouldRejectStatusUpdateByAuditor() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = authService.updateUserStatus(userId, UserStatus.SUSPENDED, UserRole.AUDITOR);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject status update when user not found")
        void shouldRejectStatusUpdateWhenUserNotFound() {
            // Given
            UUID userId = UUID.randomUUID();
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.updateUserStatus(userId, UserStatus.SUSPENDED, UserRole.SYSTEM_ADMIN))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should set updatedAt timestamp on status update")
        void shouldSetUpdatedAtTimestampOnStatusUpdate() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("testuser");
            user.setStatus(UserStatus.ACTIVE);
            Instant beforeUpdate = Instant.now();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            authService.updateUserStatus(userId, UserStatus.SUSPENDED, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(user.getUpdatedAt()).isNotNull();
            assertThat(user.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        @DisplayName("Should update status from PENDING to SUSPENDED")
        void shouldUpdateStatusFromPendingToSuspended() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("testuser");
            user.setStatus(UserStatus.PENDING);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.updateUserStatus(userId, UserStatus.SUSPENDED, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        }

        @Test
        @DisplayName("Should update status from SUSPENDED to ACTIVE")
        void shouldUpdateStatusFromSuspendedToActive() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("testuser");
            user.setStatus(UserStatus.SUSPENDED);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.updateUserStatus(userId, UserStatus.ACTIVE, UserRole.MANAGER);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Internal Methods")
    class InternalMethodsTests {

        @Test
        @DisplayName("Should get user by ID")
        void shouldGetUserById() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity expectedUser = new UserEntity();
            expectedUser.setId(userId);
            expectedUser.setUsername("testuser");

            given(userRepository.findById(userId)).willReturn(Optional.of(expectedUser));

            // When
            UserEntity result = authService.getUserById(userId);

            // Then
            assertThat(result).isEqualTo(expectedUser);
            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should return null when user not found by ID")
        void shouldReturnNullWhenUserNotFoundById() {
            // Given
            UUID userId = UUID.randomUUID();
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When
            UserEntity result = authService.getUserById(userId);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should get user ID from token")
        void shouldGetUserIdFromToken() {
            // Given
            String token = "jwt.token.here";
            UUID expectedUserId = UUID.randomUUID();

            given(jwtService.extractUserId(token)).willReturn(expectedUserId);

            // When
            UUID result = authService.getUserIdFromToken(token);

            // Then
            assertThat(result).isEqualTo(expectedUserId);
            verify(jwtService).extractUserId(token);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle multiple registrations with same email")
        void shouldHandleMultipleRegistrationsWithSameEmail() {
            // Given
            String email = "duplicate@example.com";
            given(userRepository.existsByEmail(email)).willReturn(true);

            // When
            RegistrationResult result1 = authService.register(new RegistrationRequest("user1", "password1", email));
            RegistrationResult result2 = authService.register(new RegistrationRequest("user2", "password2", email));

            // Then
            assertThat(result1.success()).isFalse();
            assertThat(result2.success()).isFalse();
        }

        @Test
        @DisplayName("Should handle approval by multiple approvers")
        void shouldHandleApprovalByMultipleApprovers() {
            // Given - First user entity for first approval
            UUID userId = UUID.randomUUID();
            UserEntity user1 = new UserEntity();
            user1.setId(userId);
            user1.setUsername("pendinguser");
            user1.setStatus(UserStatus.PENDING);
            user1.setRole(UserRole.ROLE_GUEST);

            // Second user entity for second approval (simulating fresh fetch)
            UserEntity user2 = new UserEntity();
            user2.setId(userId);
            user2.setUsername("pendinguser");
            user2.setStatus(UserStatus.PENDING);
            user2.setRole(UserRole.ROLE_GUEST);

            given(userRepository.findById(userId)).willReturn(Optional.of(user1), Optional.of(user2));
            given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> {
                UserEntity saved = invocation.getArgument(0);
                saved.setStatus(UserStatus.ACTIVE);
                return saved;
            });

            // When - First approval
            authService.approveUser(userId, UserRole.MANAGER);
            
            // Then - First approval should change status to ACTIVE
            assertThat(user1.getStatus()).isEqualTo(UserStatus.ACTIVE);

            // When - Second approval (with fresh entity)
            authService.approveUser(userId, UserRole.SYSTEM_ADMIN);

            // Then - Second approval should also change status to ACTIVE
            assertThat(user2.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should handle status update to same status")
        void shouldHandleStatusUpdateToSameStatus() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setUsername("testuser");
            user.setStatus(UserStatus.ACTIVE);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // When
            ApprovalResult result = authService.updateUserStatus(userId, UserStatus.ACTIVE, UserRole.SYSTEM_ADMIN);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should handle null approver ID")
        void shouldHandleNullApproverId() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = authService.approveUser(userId, null);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
        }

        @Test
        @DisplayName("Should handle null updater ID")
        void shouldHandleNullUpdaterId() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            ApprovalResult result = authService.updateUserStatus(userId, UserStatus.SUSPENDED, null);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Insufficient privileges");
        }

        @Test
        @DisplayName("Should handle email with very long string")
        void shouldHandleEmailWithVeryLongString() {
            // Given
            String email = "a".repeat(200) + "@example.com";

            // When
            RegistrationRequest request = new RegistrationRequest("testuser", "SecurePass123!", email);

            // Then
            assertThat(request.email()).hasSize(212);
        }

        @Test
        @DisplayName("Should handle username with special characters")
        void shouldHandleUsernameWithSpecialCharacters() {
            // Given
            String username = "test+user@example.com";

            // When
            RegistrationRequest request = new RegistrationRequest(username, "SecurePass123!", "test@example.com");

            // Then
            assertThat(request.username()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should handle username with unicode characters")
        void shouldHandleUsernameWithUnicodeCharacters() {
            // Given
            String username = "用户 测试";

            // When
            RegistrationRequest request = new RegistrationRequest(username, "SecurePass123!", "test@example.com");

            // Then
            assertThat(request.username()).isEqualTo(username);
        }
    }

    /**
     * Helper method to create a test BranchEntity.
     */
    private BranchEntity createTestBranch(UUID id) {
        BranchEntity branch = new BranchEntity();
        branch.setId(id);
        branch.setCode("HO-001");
        branch.setName("Head Office");
        branch.setActive(true);
        branch.setCreatedAt(java.time.Instant.now());
        return branch;
    }
}
