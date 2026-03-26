package com.neobank.auth.internal;

import com.neobank.auth.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for CustomUserDetailsService using JUnit 5 and Mockito.
 * Tests user loading and authority mapping.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Nested
    @DisplayName("Load User By Username")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should load active user successfully")
        void shouldLoadActiveUserSuccessfully() {
            // Given
            String username = "testuser";
            UUID userId = UUID.randomUUID();

            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setUsername(username);
            userEntity.setEmail("test@example.com");
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRole(UserRole.CUSTOMER_RETAIL);
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));
            userEntity.setStatus(UserStatus.ACTIVE);

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUsername()).isEqualTo(username);
            assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        }

        @Test
        @DisplayName("Should load user with correct authorities")
        void shouldLoadUserWithCorrectAuthorities() {
            // Given
            String username = "testuser";
            UUID userId = UUID.randomUUID();

            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRole(UserRole.CUSTOMER_RETAIL);
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL, UserRole.TELLER));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).hasSize(2);
            assertThat(userDetails.getAuthorities()).contains(
                    new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL"),
                    new SimpleGrantedAuthority("ROLE_TELLER")
            );
        }

        @Test
        @DisplayName("Should load SYSTEM_ADMIN user with correct authority")
        void shouldLoadSystemAdminUserWithCorrectAuthority() {
            // Given
            String username = "adminuser";
            UUID userId = UUID.randomUUID();

            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRole(UserRole.SYSTEM_ADMIN);
            userEntity.setRoles(List.of(UserRole.SYSTEM_ADMIN));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).contains(
                    new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")
            );
        }

        @Test
        @DisplayName("Should load MANAGER user with correct authority")
        void shouldLoadManagerUserWithCorrectAuthority() {
            // Given
            String username = "manageruser";
            UUID userId = UUID.randomUUID();

            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRole(UserRole.MANAGER);
            userEntity.setRoles(List.of(UserRole.MANAGER));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).contains(
                    new SimpleGrantedAuthority("ROLE_MANAGER")
            );
        }

        @Test
        @DisplayName("Should load TELLER user with correct authority")
        void shouldLoadTellerUserWithCorrectAuthority() {
            // Given
            String username = "telleruser";
            UUID userId = UUID.randomUUID();

            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRole(UserRole.TELLER);
            userEntity.setRoles(List.of(UserRole.TELLER));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).contains(
                    new SimpleGrantedAuthority("ROLE_TELLER")
            );
        }

        @Test
        @DisplayName("Should load AUDITOR user with correct authority")
        void shouldLoadAuditorUserWithCorrectAuthority() {
            // Given
            String username = "auditoruser";
            UUID userId = UUID.randomUUID();

            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRole(UserRole.AUDITOR);
            userEntity.setRoles(List.of(UserRole.AUDITOR));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).contains(
                    new SimpleGrantedAuthority("ROLE_AUDITOR")
            );
        }

        @Test
        @DisplayName("Should load user with ROLE_GUEST")
        void shouldLoadUserWithRoleGuest() {
            // Given
            String username = "guestuser";
            UUID userId = UUID.randomUUID();

            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRole(UserRole.ROLE_GUEST);
            userEntity.setRoles(List.of(UserRole.ROLE_GUEST));
            userEntity.setStatus(UserStatus.PENDING);

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).contains(
                    new SimpleGrantedAuthority("ROLE_ROLE_GUEST")
            );
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
            // Given
            String username = "nonexistent";
            given(userRepository.findByUsername(username)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("User not found: " + username);
        }

        @Test
        @DisplayName("Should load user with multiple roles")
        void shouldLoadUserWithMultipleRoles() {
            // Given
            String username = "multiroleuser";
            UUID userId = UUID.randomUUID();

            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRole(UserRole.MANAGER);
            userEntity.setRoles(List.of(UserRole.MANAGER, UserRole.TELLER, UserRole.AUDITOR));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).hasSize(3);
            assertThat(userDetails.getAuthorities()).contains(
                    new SimpleGrantedAuthority("ROLE_MANAGER"),
                    new SimpleGrantedAuthority("ROLE_TELLER"),
                    new SimpleGrantedAuthority("ROLE_AUDITOR")
            );
        }

        @Test
        @DisplayName("Should load user with account not expired")
        void shouldLoadUserWithAccountNotExpired() {
            // Given
            String username = "testuser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.isAccountNonExpired()).isTrue();
        }

        @Test
        @DisplayName("Should load user with account not locked")
        void shouldLoadUserWithAccountNotLocked() {
            // Given
            String username = "testuser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("Should load user with credentials not expired")
        void shouldLoadUserWithCredentialsNotExpired() {
            // Given
            String username = "testuser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        }

        @Test
        @DisplayName("Should load enabled user")
        void shouldLoadEnabledUser() {
            // Given
            String username = "testuser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null username")
        void shouldHandleNullUsername() {
            // Given
            given(userRepository.findByUsername(null)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername(null))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        @DisplayName("Should handle empty username")
        void shouldHandleEmptyUsername() {
            // Given
            given(userRepository.findByUsername("")).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername(""))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        @DisplayName("Should handle username with special characters")
        void shouldHandleUsernameWithSpecialCharacters() {
            // Given
            String username = "test+user@example.com";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should handle username with unicode characters")
        void shouldHandleUsernameWithUnicodeCharacters() {
            // Given
            String username = "用户 测试";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should handle very long username")
        void shouldHandleVeryLongUsername() {
            // Given
            String username = "a".repeat(100);
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getUsername()).hasSize(100);
        }

        @Test
        @DisplayName("Should handle user with empty roles list")
        void shouldHandleUserWithEmptyRolesList() {
            // Given
            String username = "norolesuser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(List.of());

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("Should handle user with null roles")
        void shouldHandleUserWithNullRoles() {
            // Given
            String username = "nullrolesuser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(null);

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When/Then
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle user with PENDING status")
        void shouldHandleUserWithPendingStatus() {
            // Given
            String username = "pendinguser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setStatus(UserStatus.PENDING);
            userEntity.setRoles(List.of(UserRole.ROLE_GUEST));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails).isNotNull();
            // Note: Status check is done at authentication, not at user loading
        }

        @Test
        @DisplayName("Should handle user with SUSPENDED status")
        void shouldHandleUserWithSuspendedStatus() {
            // Given
            String username = "suspendeduser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setStatus(UserStatus.SUSPENDED);
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails).isNotNull();
            // Note: Status check is done at authentication, not at user loading
        }

        @Test
        @DisplayName("Should handle user with CUSTOMER_BUSINESS role")
        void shouldHandleUserWithCustomerBusinessRole() {
            // Given
            String username = "businessuser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRole(UserRole.CUSTOMER_BUSINESS);
            userEntity.setRoles(List.of(UserRole.CUSTOMER_BUSINESS));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).contains(
                    new SimpleGrantedAuthority("ROLE_CUSTOMER_BUSINESS")
            );
        }

        @Test
        @DisplayName("Should handle user with RELATIONSHIP_OFFICER role")
        void shouldHandleUserWithRelationshipOfficerRole() {
            // Given
            String username = "rouser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRole(UserRole.RELATIONSHIP_OFFICER);
            userEntity.setRoles(List.of(UserRole.RELATIONSHIP_OFFICER));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).contains(
                    new SimpleGrantedAuthority("ROLE_RELATIONSHIP_OFFICER")
            );
        }
    }

    @Nested
    @DisplayName("UserDetails Mapping")
    class UserDetailsMappingTests {

        @Test
        @DisplayName("Should map UserEntity to Spring Security UserDetails")
        void shouldMapUserEntityToSpringSecurityUserDetails() {
            // Given
            String username = "testuser";
            String password = "encodedPassword";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash(password);
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails).isInstanceOf(User.class);
            assertThat(userDetails.getUsername()).isEqualTo(username);
            assertThat(userDetails.getPassword()).isEqualTo(password);
        }

        @Test
        @DisplayName("Should map multiple roles to authorities")
        void shouldMapMultipleRolesToAuthorities() {
            // Given
            String username = "testuser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(List.of(
                    UserRole.CUSTOMER_RETAIL,
                    UserRole.TELLER,
                    UserRole.MANAGER
            ));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).hasSize(3);
        }

        @Test
        @DisplayName("Should prefix roles with ROLE_")
        void shouldPrefixRolesWithRole() {
            // Given
            String username = "testuser";
            UserEntity userEntity = new UserEntity();
            userEntity.setId(UUID.randomUUID());
            userEntity.setUsername(username);
            userEntity.setPasswordHash("encodedPassword");
            userEntity.setRoles(List.of(UserRole.CUSTOMER_RETAIL));

            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));

            // When
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Then
            assertThat(userDetails.getAuthorities()).first()
                    .extracting(a -> a.getAuthority())
                    .isEqualTo("ROLE_CUSTOMER_RETAIL");
        }
    }
}
