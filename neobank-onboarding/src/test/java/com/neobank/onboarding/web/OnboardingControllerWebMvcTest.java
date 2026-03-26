package com.neobank.onboarding.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neobank.auth.UserRole;
import com.neobank.onboarding.ApprovalResult;
import com.neobank.onboarding.UserStatus;
import com.neobank.onboarding.internal.OnboardingService;
import com.neobank.onboarding.internal.UserProfileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc test for OnboardingController.
 * Tests REST endpoints, security, and error handling.
 */
@WebMvcTest(OnboardingController.class)
@DisplayName("OnboardingController WebMvc Tests")
class OnboardingControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("POST /api/onboarding/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";
            String password = "SecurePass123!";

            OnboardingService.OnboardingResult result = OnboardingService.OnboardingResult.success(
                    userId, "Registration successful. Awaiting approval."
            );

            given(onboardingService.registerUser(username, email, password)).willReturn(result);

            Map<String, String> request = Map.of(
                    "username", username,
                    "email", email,
                    "password", password
            );

            // When/Then
            mockMvc.perform(post("/api/onboarding/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.message").value("Registration successful. Awaiting approval."));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for duplicate email")
        void shouldReturn400BadRequestForDuplicateEmail() throws Exception {
            // Given
            String username = "testuser";
            String email = "existing@example.com";
            String password = "SecurePass123!";

            OnboardingService.OnboardingResult result = OnboardingService.OnboardingResult.failure(
                    "Email already registered: " + email
            );

            given(onboardingService.registerUser(username, email, password)).willReturn(result);

            Map<String, String> request = Map.of(
                    "username", username,
                    "email", email,
                    "password", password
            );

            // When/Then
            mockMvc.perform(post("/api/onboarding/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Email already registered: " + email));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for empty payload")
        void shouldReturn400BadRequestForEmptyPayload() throws Exception {
            // Given
            String emptyRequest = "{}";

            // When/Then
            mockMvc.perform(post("/api/onboarding/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(emptyRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 415 Unsupported Media Type for wrong content type")
        void shouldReturn415UnsupportedMediaTypeForWrongContentType() throws Exception {
            // Given
            Map<String, String> request = Map.of(
                    "username", "testuser",
                    "email", "test@example.com",
                    "password", "SecurePass123!"
            );

            // When/Then
            mockMvc.perform(post("/api/onboarding/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .content("username=testuser&email=test@example.com&password=SecurePass123!"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("PUT /api/onboarding/users/{id}/approve")
    class ApproveUserEndpointTests {

        @Test
        @DisplayName("Should approve user successfully by MANAGER")
        @WithMockUser(username = "manager", roles = {"MANAGER"})
        void shouldApproveUserSuccessfullyByManager() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            UUID approverId = UUID.randomUUID();
            ApprovalResult result = ApprovalResult.success(userId, "User approved and activated");

            given(onboardingService.approveUser(eq(userId), any(UUID.class), eq(UserRole.MANAGER)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(put("/api/onboarding/users/{id}/approve", userId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.message").value("User approved and activated"));
        }

        @Test
        @DisplayName("Should approve user successfully by RELATIONSHIP_OFFICER")
        @WithMockUser(username = "ro", roles = {"RELATIONSHIP_OFFICER"})
        void shouldApproveUserSuccessfullyByRelationshipOfficer() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            ApprovalResult result = ApprovalResult.success(userId, "User approved and activated");

            given(onboardingService.approveUser(eq(userId), any(UUID.class), eq(UserRole.RELATIONSHIP_OFFICER)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(put("/api/onboarding/users/{id}/approve", userId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should approve user successfully by SYSTEM_ADMIN")
        @WithMockUser(username = "admin", roles = {"SYSTEM_ADMIN"})
        void shouldApproveUserSuccessfullyBySystemAdmin() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            ApprovalResult result = ApprovalResult.success(userId, "User approved and activated");

            given(onboardingService.approveUser(eq(userId), any(UUID.class), eq(UserRole.SYSTEM_ADMIN)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(put("/api/onboarding/users/{id}/approve", userId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for insufficient privileges")
        @WithMockUser(username = "customer", roles = {"CUSTOMER_RETAIL"})
        void shouldReturn400BadRequestForInsufficientPrivileges() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            ApprovalResult result = ApprovalResult.failure("Insufficient privileges for user approval");

            given(onboardingService.approveUser(eq(userId), any(UUID.class), eq(UserRole.CUSTOMER_RETAIL)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(put("/api/onboarding/users/{id}/approve", userId)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Insufficient privileges for user approval"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when user profile not found")
        @WithMockUser(username = "manager", roles = {"MANAGER"})
        void shouldReturn400BadRequestWhenUserProfileNotFound() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            ApprovalResult result = ApprovalResult.failure("User profile not found");

            given(onboardingService.approveUser(eq(userId), any(UUID.class), eq(UserRole.MANAGER)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(put("/api/onboarding/users/{id}/approve", userId)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User profile not found"));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when not authenticated")
        void shouldReturn401UnauthorizedWhenNotAuthenticated() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();

            // When/Then
            mockMvc.perform(put("/api/onboarding/users/{id}/approve", userId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid UUID format")
        @WithMockUser(username = "manager", roles = {"MANAGER"})
        void shouldReturn400BadRequestForInvalidUuidFormat() throws Exception {
            // When/Then
            mockMvc.perform(put("/api/onboarding/users/{id}/approve", "invalid-uuid")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/onboarding/users/{id}/status")
    class UpdateUserStatusEndpointTests {

        @Test
        @DisplayName("Should update user status to SUSPENDED by SYSTEM_ADMIN")
        @WithMockUser(username = "admin", roles = {"SYSTEM_ADMIN"})
        void shouldUpdateUserStatusToSuspendedBySystemAdmin() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            ApprovalResult result = ApprovalResult.success(userId, "User status updated to SUSPENDED");

            given(onboardingService.updateUserStatus(eq(userId), eq(UserStatus.SUSPENDED), any(UserRole.class)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(patch("/api/onboarding/users/{id}/status", userId)
                            .with(csrf())
                            .param("status", "SUSPENDED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User status updated to SUSPENDED"));
        }

        @Test
        @DisplayName("Should update user status to ACTIVE by MANAGER")
        @WithMockUser(username = "manager", roles = {"MANAGER"})
        void shouldUpdateUserStatusToActiveByManager() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            ApprovalResult result = ApprovalResult.success(userId, "User status updated to ACTIVE");

            given(onboardingService.updateUserStatus(eq(userId), eq(UserStatus.ACTIVE), any(UserRole.class)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(patch("/api/onboarding/users/{id}/status", userId)
                            .with(csrf())
                            .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for insufficient privileges")
        @WithMockUser(username = "customer", roles = {"CUSTOMER_RETAIL"})
        void shouldReturn400BadRequestForInsufficientPrivilegesForStatusUpdate() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            ApprovalResult result = ApprovalResult.failure("Insufficient privileges to update user status");

            given(onboardingService.updateUserStatus(eq(userId), eq(UserStatus.SUSPENDED), any(UserRole.class)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(patch("/api/onboarding/users/{id}/status", userId)
                            .with(csrf())
                            .param("status", "SUSPENDED"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Insufficient privileges to update user status"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when status parameter is missing")
        @WithMockUser(username = "admin", roles = {"SYSTEM_ADMIN"})
        void shouldReturn400BadRequestWhenStatusParameterIsMissing() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();

            // When/Then
            mockMvc.perform(patch("/api/onboarding/users/{id}/status", userId)
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when not authenticated for status update")
        void shouldReturn401UnauthorizedWhenNotAuthenticatedForStatusUpdate() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();

            // When/Then
            mockMvc.perform(patch("/api/onboarding/users/{id}/status", userId)
                            .param("status", "SUSPENDED"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/onboarding/me")
    class GetMyStatusEndpointTests {

        @Test
        @DisplayName("Should get current user's onboarding status")
        @WithMockUser(username = "testuser", roles = {"CUSTOMER_RETAIL"})
        void shouldGetCurrentUsersOnboardingStatus() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setEmail("test@example.com");
            profile.setStatus(UserStatus.ACTIVE);
            profile.setKycVerified(true);
            profile.setMustChangePassword(false);

            given(onboardingService.getUserProfile(any(UUID.class))).willReturn(Optional.of(profile));

            // When/Then
            mockMvc.perform(get("/api/onboarding/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.kycVerified").value(true))
                    .andExpect(jsonPath("$.mustChangePassword").value(false));
        }

        @Test
        @DisplayName("Should return 404 Not Found when user profile not found")
        @WithMockUser(username = "testuser", roles = {"CUSTOMER_RETAIL"})
        void shouldReturn404NotFoundWhenUserProfileNotFound() throws Exception {
            // Given
            given(onboardingService.getUserProfile(any(UUID.class))).willReturn(Optional.empty());

            // When/Then
            mockMvc.perform(get("/api/onboarding/me")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when not authenticated")
        void shouldReturn401UnauthorizedWhenNotAuthenticatedForMe() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/onboarding/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.authenticated").value(false));
        }

        @Test
        @DisplayName("Should get status for PENDING user")
        @WithMockUser(username = "pendinguser", roles = {"CUSTOMER_RETAIL"})
        void shouldGetStatusForPendingUser() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setEmail("pending@example.com");
            profile.setStatus(UserStatus.PENDING);
            profile.setKycVerified(false);
            profile.setMustChangePassword(false);

            given(onboardingService.getUserProfile(any(UUID.class))).willReturn(Optional.of(profile));

            // When/Then
            mockMvc.perform(get("/api/onboarding/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.kycVerified").value(false));
        }
    }

    @Nested
    @DisplayName("Response Format")
    class ResponseFormatTests {

        @Test
        @DisplayName("Should return proper JSON structure for registration")
        void shouldReturnProperJsonStructureForRegistration() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            OnboardingService.OnboardingResult result = OnboardingService.OnboardingResult.success(
                    userId, "Registration successful"
            );

            given(onboardingService.registerUser(any(), any(), any())).willReturn(result);

            Map<String, String> request = Map.of(
                    "username", "testuser",
                    "email", "test@example.com",
                    "password", "SecurePass123!"
            );

            // When/Then
            mockMvc.perform(post("/api/onboarding/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(jsonPath("$.success").exists())
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return JSON content type")
        @WithMockUser(username = "testuser", roles = {"CUSTOMER_RETAIL"})
        void shouldReturnJsonContentType() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            UserProfileEntity profile = new UserProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setUserId(userId);
            profile.setEmail("test@example.com");
            profile.setStatus(UserStatus.ACTIVE);

            given(onboardingService.getUserProfile(any(UUID.class))).willReturn(Optional.of(profile));

            // When/Then
            mockMvc.perform(get("/api/onboarding/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("Security Headers")
    class SecurityHeadersTests {

        @Test
        @DisplayName("Should reject POST without CSRF token")
        void shouldRejectPostWithoutCsrfToken() throws Exception {
            // Given
            Map<String, String> request = Map.of(
                    "username", "testuser",
                    "email", "test@example.com",
                    "password", "SecurePass123!"
            );

            // When/Then
            mockMvc.perform(post("/api/onboarding/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject PUT without CSRF token")
        @WithMockUser(username = "manager", roles = {"MANAGER"})
        void shouldRejectPutWithoutCsrfToken() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();

            // When/Then
            mockMvc.perform(put("/api/onboarding/users/{id}/approve", userId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should accept POST with CSRF token")
        void shouldAcceptPostWithCsrfToken() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            OnboardingService.OnboardingResult result = OnboardingService.OnboardingResult.success(
                    userId, "Registration successful"
            );

            given(onboardingService.registerUser(any(), any(), any())).willReturn(result);

            Map<String, String> request = Map.of(
                    "username", "testuser",
                    "email", "test@example.com",
                    "password", "SecurePass123!"
            );

            // When/Then
            mockMvc.perform(post("/api/onboarding/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle username with special characters")
        void shouldHandleUsernameWithSpecialCharacters() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "John O'Brien-Smith";
            String email = "test@example.com";
            String password = "SecurePass123!";

            OnboardingService.OnboardingResult result = OnboardingService.OnboardingResult.success(
                    userId, "Registration successful"
            );

            given(onboardingService.registerUser(username, email, password)).willReturn(result);

            Map<String, String> request = Map.of(
                    "username", username,
                    "email", email,
                    "password", password
            );

            // When/Then
            mockMvc.perform(post("/api/onboarding/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test+user@example.com";
            String password = "SecurePass123!";

            OnboardingService.OnboardingResult result = OnboardingService.OnboardingResult.success(
                    userId, "Registration successful"
            );

            given(onboardingService.registerUser(username, email, password)).willReturn(result);

            Map<String, String> request = Map.of(
                    "username", username,
                    "email", email,
                    "password", password
            );

            // When/Then
            mockMvc.perform(post("/api/onboarding/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle long email")
        void shouldHandleLongEmail() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "a".repeat(200) + "@example.com";
            String password = "SecurePass123!";

            OnboardingService.OnboardingResult result = OnboardingService.OnboardingResult.success(
                    userId, "Registration successful"
            );

            given(onboardingService.registerUser(username, email, password)).willReturn(result);

            Map<String, String> request = Map.of(
                    "username", username,
                    "email", email,
                    "password", password
            );

            // When/Then
            mockMvc.perform(post("/api/onboarding/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
}
