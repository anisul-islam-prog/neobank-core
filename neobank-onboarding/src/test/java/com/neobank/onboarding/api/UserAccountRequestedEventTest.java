package com.neobank.onboarding.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for UserAccountRequestedEvent record using JUnit 5.
 * Tests domain event record and factory methods.
 */
@DisplayName("UserAccountRequestedEvent Unit Tests")
class UserAccountRequestedEventTest {

    @Nested
    @DisplayName("Event Creation")
    class EventCreationTests {

        @Test
        @DisplayName("Should create event with all required fields")
        void shouldCreateEventWithAllRequiredFields() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";
            String occurredAt = "2024-01-15T10:30:00Z";

            // When
            UserAccountRequestedEvent event = new UserAccountRequestedEvent(userId, username, email, occurredAt);

            // Then
            assertThat(event.userId()).isEqualTo(userId);
            assertThat(event.username()).isEqualTo(username);
            assertThat(event.email()).isEqualTo(email);
            assertThat(event.occurredAt()).isEqualTo(occurredAt);
        }

        @Test
        @DisplayName("Should create event using factory method")
        void shouldCreateEventUsingFactoryMethod() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";

            // When
            UserAccountRequestedEvent event = UserAccountRequestedEvent.of(userId, username, email);

            // Then
            assertThat(event.userId()).isEqualTo(userId);
            assertThat(event.username()).isEqualTo(username);
            assertThat(event.email()).isEqualTo(email);
            assertThat(event.occurredAt()).isNotNull();
        }

        @Test
        @DisplayName("Should set occurredAt timestamp in factory method")
        void shouldSetOccurredAtTimestampInFactoryMethod() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";

            // When
            UserAccountRequestedEvent event = UserAccountRequestedEvent.of(userId, username, email);

            // Then
            assertThat(event.occurredAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Field Validation")
    class FieldValidationTests {

        @Test
        @DisplayName("Should throw exception when userId is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new UserAccountRequestedEvent(
                    null, "testuser", "test@example.com", "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("userId must not be null");
        }

        @Test
        @DisplayName("Should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new UserAccountRequestedEvent(
                    UUID.randomUUID(), null, "test@example.com", "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when username is blank")
        void shouldThrowExceptionWhenUsernameIsBlank() {
            // Given/When/Then
            assertThatThrownBy(() -> new UserAccountRequestedEvent(
                    UUID.randomUUID(), "   ", "test@example.com", "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new UserAccountRequestedEvent(
                    UUID.randomUUID(), "testuser", null, "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when email is blank")
        void shouldThrowExceptionWhenEmailIsBlank() {
            // Given/When/Then
            assertThatThrownBy(() -> new UserAccountRequestedEvent(
                    UUID.randomUUID(), "testuser", "   ", "2024-01-15T10:30:00Z"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email must not be blank");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle username with special characters")
        void shouldHandleUsernameWithSpecialCharacters() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "John O'Brien-Smith";
            String email = "test@example.com";

            // When
            UserAccountRequestedEvent event = UserAccountRequestedEvent.of(userId, username, email);

            // Then
            assertThat(event.username()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should handle username with unicode characters")
        void shouldHandleUsernameWithUnicodeCharacters() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "用户 测试";
            String email = "test@example.com";

            // When
            UserAccountRequestedEvent event = UserAccountRequestedEvent.of(userId, username, email);

            // Then
            assertThat(event.username()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should handle email with special characters")
        void shouldHandleEmailWithSpecialCharacters() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test+user@example.com";

            // When
            UserAccountRequestedEvent event = UserAccountRequestedEvent.of(userId, username, email);

            // Then
            assertThat(event.email()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should handle email with unicode characters")
        void shouldHandleEmailWithUnicodeCharacters() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "用户@example.com";

            // When
            UserAccountRequestedEvent event = UserAccountRequestedEvent.of(userId, username, email);

            // Then
            assertThat(event.email()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should handle long username")
        void shouldHandleLongUsername() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "a".repeat(100);
            String email = "test@example.com";

            // When
            UserAccountRequestedEvent event = UserAccountRequestedEvent.of(userId, username, email);

            // Then
            assertThat(event.username()).hasSize(100);
        }

        @Test
        @DisplayName("Should handle long email")
        void shouldHandleLongEmail() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "a".repeat(200) + "@example.com";

            // When
            UserAccountRequestedEvent event = UserAccountRequestedEvent.of(userId, username, email);

            // Then
            assertThat(event.email()).hasSize(212);
        }

        @Test
        @DisplayName("Should handle ISO 8601 timestamp")
        void shouldHandleIso8601Timestamp() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";
            String timestamp = "2024-01-15T10:30:00.123Z";

            // When
            UserAccountRequestedEvent event = new UserAccountRequestedEvent(userId, username, email, timestamp);

            // Then
            assertThat(event.occurredAt()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("Should handle epoch timestamp")
        void shouldHandleEpochTimestamp() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";
            String timestamp = String.valueOf(System.currentTimeMillis());

            // When
            UserAccountRequestedEvent event = new UserAccountRequestedEvent(userId, username, email, timestamp);

            // Then
            assertThat(event.occurredAt()).isEqualTo(timestamp);
        }
    }

    @Nested
    @DisplayName("Record Accessors")
    class RecordAccessorsTests {

        @Test
        @DisplayName("Should access all fields correctly")
        void shouldAccessAllFieldsCorrectly() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";
            String occurredAt = "2024-01-15T10:30:00Z";

            // When
            UserAccountRequestedEvent event = new UserAccountRequestedEvent(userId, username, email, occurredAt);

            // Then
            assertThat(event.userId()).isEqualTo(userId);
            assertThat(event.username()).isEqualTo(username);
            assertThat(event.email()).isEqualTo(email);
            assertThat(event.occurredAt()).isEqualTo(occurredAt);
        }

        @Test
        @DisplayName("Should equal another event with same fields")
        void shouldEqualAnotherEventWithSameFields() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";
            String occurredAt = "2024-01-15T10:30:00Z";

            UserAccountRequestedEvent event1 = new UserAccountRequestedEvent(userId, username, email, occurredAt);
            UserAccountRequestedEvent event2 = new UserAccountRequestedEvent(userId, username, email, occurredAt);

            // Then
            assertThat(event1).isEqualTo(event2);
        }

        @Test
        @DisplayName("Should not equal event with different userId")
        void shouldNotEqualEventWithDifferentUserId() {
            // Given
            String username = "testuser";
            String email = "test@example.com";
            String occurredAt = "2024-01-15T10:30:00Z";

            UserAccountRequestedEvent event1 = new UserAccountRequestedEvent(UUID.randomUUID(), username, email, occurredAt);
            UserAccountRequestedEvent event2 = new UserAccountRequestedEvent(UUID.randomUUID(), username, email, occurredAt);

            // Then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";
            String occurredAt = "2024-01-15T10:30:00Z";

            UserAccountRequestedEvent event = new UserAccountRequestedEvent(userId, username, email, occurredAt);

            // Then
            assertThat(event.hashCode()).isNotNull();
            assertThat(event.hashCode()).isEqualTo(event.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString format")
        void shouldHaveCorrectToStringFormat() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";
            String occurredAt = "2024-01-15T10:30:00Z";

            UserAccountRequestedEvent event = new UserAccountRequestedEvent(userId, username, email, occurredAt);

            // Then
            assertThat(event.toString()).contains("UserAccountRequestedEvent");
            assertThat(event.toString()).contains(username);
        }

        @Test
        @DisplayName("Should include email in toString")
        void shouldIncludeEmailInToString() {
            // Given
            UUID userId = UUID.randomUUID();
            String username = "testuser";
            String email = "test@example.com";
            String occurredAt = "2024-01-15T10:30:00Z";

            UserAccountRequestedEvent event = new UserAccountRequestedEvent(userId, username, email, occurredAt);

            // Then
            assertThat(event.toString()).contains(email);
        }
    }
}
