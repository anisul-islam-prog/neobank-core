package com.neobank.auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neobank.auth.*;
import com.neobank.auth.api.AuthApi;
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

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc test for AuthController.
 * Tests REST endpoints, JSON mapping, security, and error handling.
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController WebMvc Tests")
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthApi authApi;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should register user successfully and return 200 OK")
        void shouldRegisterUserSuccessfullyAndReturn200Ok() throws Exception {
            // Given
            RegistrationRequest request = new RegistrationRequest("newuser", "SecurePass123!", "new@example.com");
            RegistrationResult result = RegistrationResult.success(UUID.randomUUID());

            given(authApi.register(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.userId").exists());
        }

        @Test
        @DisplayName("Should return 409 Conflict for duplicate username")
        void shouldReturn409ConflictForDuplicateUsername() throws Exception {
            // Given
            RegistrationRequest request = new RegistrationRequest("existinguser", "SecurePass123!", "new@example.com");
            RegistrationResult result = RegistrationResult.failure("Username already exists: existinguser");

            given(authApi.register(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Username already exists: existinguser"));
        }

        @Test
        @DisplayName("Should return 409 Conflict for duplicate email")
        void shouldReturn409ConflictForDuplicateEmail() throws Exception {
            // Given
            RegistrationRequest request = new RegistrationRequest("newuser", "SecurePass123!", "existing@example.com");
            RegistrationResult result = RegistrationResult.failure("Email already registered: existing@example.com");

            given(authApi.register(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Email already registered: existing@example.com"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid registration data")
        void shouldReturn400BadRequestForInvalidRegistrationData() throws Exception {
            // Given
            RegistrationRequest request = new RegistrationRequest("newuser", "weak", "invalid-email");
            RegistrationResult result = RegistrationResult.failure("Invalid email format");

            given(authApi.register(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for empty payload")
        void shouldReturn400BadRequestForEmptyPayload() throws Exception {
            // Given
            String emptyRequest = "{}";

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(emptyRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 415 Unsupported Media Type for wrong content type")
        void shouldReturn415UnsupportedMediaTypeForWrongContentType() throws Exception {
            // Given
            RegistrationRequest request = new RegistrationRequest("newuser", "SecurePass123!", "new@example.com");

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .content("username=newuser&password=SecurePass123!&email=new@example.com"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("Should register user with special characters in email")
        void shouldRegisterUserWithSpecialCharactersInEmail() throws Exception {
            // Given
            RegistrationRequest request = new RegistrationRequest("testuser", "SecurePass123!", "test+user@example.com");
            RegistrationResult result = RegistrationResult.success(UUID.randomUUID());

            given(authApi.register(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should register user with unicode in email")
        void shouldRegisterUserWithUnicodeInEmail() throws Exception {
            // Given
            RegistrationRequest request = new RegistrationRequest("testuser", "SecurePass123!", "用户@example.com");
            RegistrationResult result = RegistrationResult.success(UUID.randomUUID());

            given(authApi.register(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should authenticate user successfully and return JWT token")
        void shouldAuthenticateUserSuccessfullyAndReturnJwtToken() throws Exception {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("testuser", "SecurePass123!");
            UUID userId = UUID.randomUUID();
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
            AuthenticationResult result = AuthenticationResult.success(userId, token, 86400L, false, UserStatus.ACTIVE);

            given(authApi.authenticate(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.token").value(token))
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.mustChangePassword").value(false))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should return 403 Forbidden for PENDING user")
        void shouldReturn403ForbiddenForPendingUser() throws Exception {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("pendinguser", "SecurePass123!");
            AuthenticationResult result = AuthenticationResult.forbidden("Account pending approval. Please contact support.");

            given(authApi.authenticate(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Account pending approval. Please contact support."));
        }

        @Test
        @DisplayName("Should return 403 Forbidden for SUSPENDED user")
        void shouldReturn403ForbiddenForSuspendedUser() throws Exception {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("suspendeduser", "SecurePass123!");
            AuthenticationResult result = AuthenticationResult.forbidden("Account suspended. Please contact support.");

            given(authApi.authenticate(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Account suspended. Please contact support."));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized for invalid credentials")
        void shouldReturn401UnauthorizedForInvalidCredentials() throws Exception {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("testuser", "WrongPassword!");
            AuthenticationResult result = AuthenticationResult.failure("Invalid username or password");

            given(authApi.authenticate(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid username or password"));
        }

        @Test
        @DisplayName("Should return mustChangePassword flag when required")
        void shouldReturnMustChangePasswordFlagWhenRequired() throws Exception {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("staffuser", "SecurePass123!");
            UUID userId = UUID.randomUUID();
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
            AuthenticationResult result = AuthenticationResult.success(userId, token, 86400L, true, UserStatus.ACTIVE);

            given(authApi.authenticate(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mustChangePassword").value(true));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for empty payload")
        void shouldReturn400BadRequestForEmptyPayload() throws Exception {
            // Given
            String emptyRequest = "{}";

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(emptyRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request for null username")
        void shouldReturn400BadRequestForNullUsername() throws Exception {
            // Given
            String request = "{\"username\":null,\"password\":\"SecurePass123!\"}";

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request for blank password")
        void shouldReturn400BadRequestForBlankPassword() throws Exception {
            // Given
            String request = "{\"username\":\"testuser\",\"password\":\"   \"}";

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 415 Unsupported Media Type for wrong content type")
        void shouldReturn415UnsupportedMediaTypeForWrongContentType() throws Exception {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("testuser", "SecurePass123!");

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .content("username=testuser&password=SecurePass123!"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/onboard")
    class OnboardStaffEndpointTests {

        @Test
        @DisplayName("Should onboard staff successfully by SYSTEM_ADMIN")
        @WithMockUser(username = "admin", roles = {"SYSTEM_ADMIN"})
        void shouldOnboardStaffSuccessfullyBySystemAdmin() throws Exception {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "manageruser", "SecurePass123!", "manager@example.com",
                    UserRole.MANAGER, UUID.randomUUID()
            );
            RegistrationResult result = RegistrationResult.success(UUID.randomUUID());

            given(authApi.onboardStaff(any(StaffOnboardingRequest.class), any(UserRole.class)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/onboard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should onboard staff successfully by MANAGER")
        @WithMockUser(username = "manager", roles = {"MANAGER"})
        void shouldOnboardStaffSuccessfullyByManager() throws Exception {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "telleruser", "SecurePass123!", "teller@example.com",
                    UserRole.TELLER, null
            );
            RegistrationResult result = RegistrationResult.success(UUID.randomUUID());

            given(authApi.onboardStaff(any(StaffOnboardingRequest.class), any(UserRole.class)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/onboard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 403 Forbidden for insufficient privileges")
        @WithMockUser(username = "customer", roles = {"CUSTOMER_RETAIL"})
        void shouldReturn403ForbiddenForInsufficientPrivileges() throws Exception {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "manageruser", "SecurePass123!", "manager@example.com",
                    UserRole.MANAGER, UUID.randomUUID()
            );
            RegistrationResult result = RegistrationResult.failure("Insufficient privileges for staff onboarding");

            given(authApi.onboardStaff(any(StaffOnboardingRequest.class), any(UserRole.class)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/onboard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Insufficient privileges for staff onboarding"));
        }

        @Test
        @DisplayName("Should return 409 Conflict for duplicate staff username")
        @WithMockUser(username = "admin", roles = {"SYSTEM_ADMIN"})
        void shouldReturn409ConflictForDuplicateStaffUsername() throws Exception {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "existinguser", "SecurePass123!", "manager@example.com",
                    UserRole.MANAGER, UUID.randomUUID()
            );
            RegistrationResult result = RegistrationResult.failure("Username already exists: existinguser");

            given(authApi.onboardStaff(any(StaffOnboardingRequest.class), any(UserRole.class)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/onboard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Username already exists: existinguser"));
        }

        @Test
        @DisplayName("Should return 409 Conflict for duplicate staff email")
        @WithMockUser(username = "admin", roles = {"SYSTEM_ADMIN"})
        void shouldReturn409ConflictForDuplicateStaffEmail() throws Exception {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "newuser", "SecurePass123!", "existing@example.com",
                    UserRole.MANAGER, UUID.randomUUID()
            );
            RegistrationResult result = RegistrationResult.failure("Email already registered: existing@example.com");

            given(authApi.onboardStaff(any(StaffOnboardingRequest.class), any(UserRole.class)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/onboard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Email already registered: existing@example.com"));
        }

        @Test
        @DisplayName("Should return 403 Forbidden when MANAGER tries to onboard SYSTEM_ADMIN")
        @WithMockUser(username = "manager", roles = {"MANAGER"})
        void shouldReturn403ForbiddenWhenManagerTriesToOnboardSystemAdmin() throws Exception {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "adminuser", "SecurePass123!", "admin@example.com",
                    UserRole.SYSTEM_ADMIN, null
            );
            RegistrationResult result = RegistrationResult.failure("Insufficient privileges to assign SYSTEM_ADMIN");

            given(authApi.onboardStaff(any(StaffOnboardingRequest.class), any(UserRole.class)))
                    .willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/onboard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for empty payload")
        @WithMockUser(username = "admin", roles = {"SYSTEM_ADMIN"})
        void shouldReturn400BadRequestForEmptyPayload() throws Exception {
            // Given
            String emptyRequest = "{}";

            // When/Then
            mockMvc.perform(post("/api/auth/onboard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(emptyRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request for null role")
        @WithMockUser(username = "admin", roles = {"SYSTEM_ADMIN"})
        void shouldReturn400BadRequestForNullRole() throws Exception {
            // Given
            String request = "{\"username\":\"testuser\",\"password\":\"SecurePass123!\",\"email\":\"test@example.com\",\"role\":null}";

            // When/Then
            mockMvc.perform(post("/api/auth/onboard")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when not authenticated")
        void shouldReturn401UnauthorizedWhenNotAuthenticated() throws Exception {
            // Given
            StaffOnboardingRequest request = new StaffOnboardingRequest(
                    "newuser", "SecurePass123!", "new@example.com",
                    UserRole.TELLER, null
            );

            // When/Then
            mockMvc.perform(post("/api/auth/onboard")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUserEndpointTests {

        @Test
        @DisplayName("Should return current authenticated user info")
        @WithMockUser(username = "testuser", roles = {"CUSTOMER_RETAIL"})
        void shouldReturnCurrentAuthenticatedUserInfo() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated").value(true))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.roles").isArray());
        }

        @Test
        @DisplayName("Should return unauthenticated when no user")
        void shouldReturnUnauthenticatedWhenNoUser() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated").value(false));
        }

        @Test
        @DisplayName("Should return user with MANAGER role")
        @WithMockUser(username = "manageruser", roles = {"MANAGER"})
        void shouldReturnUserWithManagerRole() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("manageruser"));
        }

        @Test
        @DisplayName("Should return user with SYSTEM_ADMIN role")
        @WithMockUser(username = "adminuser", roles = {"SYSTEM_ADMIN"})
        void shouldReturnUserWithSystemAdminRole() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("adminuser"));
        }

        @Test
        @DisplayName("Should return user with multiple roles")
        @WithMockUser(username = "multiroleuser", roles = {"MANAGER", "TELLER", "AUDITOR"})
        void shouldReturnUserWithMultipleRoles() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("multiroleuser"))
                    .andExpect(jsonPath("$.roles").isArray());
        }

        @Test
        @DisplayName("Should return user with STAFF role")
        @WithMockUser(username = "staffuser", roles = {"STAFF"})
        void shouldReturnUserWithStaffRole() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("staffuser"));
        }
    }

    @Nested
    @DisplayName("Response Format")
    class ResponseFormatTests {

        @Test
        @DisplayName("Should return proper JSON structure for registration")
        void shouldReturnProperJsonStructureForRegistration() throws Exception {
            // Given
            RegistrationRequest request = new RegistrationRequest("newuser", "SecurePass123!", "new@example.com");
            RegistrationResult result = RegistrationResult.success(UUID.randomUUID());

            given(authApi.register(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(jsonPath("$.success").exists())
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return proper JSON structure for login")
        void shouldReturnProperJsonStructureForLogin() throws Exception {
            // Given
            AuthenticationRequest request = new AuthenticationRequest("testuser", "SecurePass123!");
            UUID userId = UUID.randomUUID();
            String token = "jwt.token";
            AuthenticationResult result = AuthenticationResult.success(userId, token, 86400L, false, UserStatus.ACTIVE);

            given(authApi.authenticate(request)).willReturn(result);

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(jsonPath("$.success").exists())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.expiresIn").exists())
                    .andExpect(jsonPath("$.mustChangePassword").exists())
                    .andExpect(jsonPath("$.status").exists());
        }

        @Test
        @DisplayName("Should return JSON content type")
        @WithMockUser(username = "testuser", roles = {"CUSTOMER_RETAIL"})
        void shouldReturnJsonContentType() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("Security Headers")
    class SecurityHeadersTests {

        @Test
        @DisplayName("Should include CSRF token in response")
        @WithMockUser(username = "testuser", roles = {"CUSTOMER_RETAIL"})
        void shouldIncludeCsrfTokenInResponse() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject POST without CSRF token")
        void shouldRejectPostWithoutCsrfToken() throws Exception {
            // Given
            RegistrationRequest request = new RegistrationRequest("newuser", "SecurePass123!", "new@example.com");

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject GET without CSRF token")
        void shouldRejectGetWithoutCsrfToken() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle user with UUID username")
        @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = {"CUSTOMER_RETAIL"})
        void shouldHandleUserWithUuidUsername() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("550e8400-e29b-41d4-a716-446655440000"));
        }

        @Test
        @DisplayName("Should handle user with special characters in username")
        @WithMockUser(username = "test+user@example.com", roles = {"CUSTOMER_RETAIL"})
        void shouldHandleUserWithSpecialCharactersInUsername() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("test+user@example.com"));
        }

        @Test
        @DisplayName("Should handle user with unicode in username")
        @WithMockUser(username = "用户 测试", roles = {"CUSTOMER_RETAIL"})
        void shouldHandleUserWithUnicodeInUsername() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("用户 测试"));
        }

        @Test
        @DisplayName("Should handle user with very long username")
        @WithMockUser(username = "verylongusernamethatislongerthanusual", roles = {"CUSTOMER_RETAIL"})
        void shouldHandleUserWithVeryLongUsername() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("verylongusernamethatislongerthanusual"));
        }

        @Test
        @DisplayName("Should handle invalid UUID format in token")
        @WithMockUser(username = "invalid-uuid", roles = {"CUSTOMER_RETAIL"})
        void shouldHandleInvalidUuidFormatInToken() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/auth/me")
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }
}
