package com.neobank.core.transfers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TransferRequest record using JUnit 5.
 * Tests record construction and validation.
 */
@DisplayName("TransferRequest Unit Tests")
class TransferRequestTest {

    @Nested
    @DisplayName("Record Construction")
    class RecordConstructionTests {

        @Test
        @DisplayName("Should create request with all fields")
        void shouldCreateRequestWithAllFields() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            UUID initiatorId = UUID.randomUUID();
            String initiatorRole = "TELLER";
            String reason = "Test transfer";

            // When
            TransferRequest request = new TransferRequest(
                    fromId, toId, amount, currency, initiatorId, initiatorRole, reason
            );

            // Then
            assertThat(request.fromId()).isEqualTo(fromId);
            assertThat(request.toId()).isEqualTo(toId);
            assertThat(request.amount()).isEqualByComparingTo(amount);
            assertThat(request.currency()).isEqualTo(currency);
            assertThat(request.initiatorId()).isEqualTo(initiatorId);
            assertThat(request.initiatorRole()).isEqualTo(initiatorRole);
            assertThat(request.reason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("Should create request with default currency (USD)")
        void shouldCreateRequestWithDefaultCurrency() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            // When
            TransferRequest request = new TransferRequest(fromId, toId, amount);

            // Then
            assertThat(request.currency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should create request with custom currency")
        void shouldCreateRequestWithCustomCurrency() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "EUR";

            // When
            TransferRequest request = new TransferRequest(fromId, toId, amount, currency);

            // Then
            assertThat(request.currency()).isEqualTo(currency);
        }

        @Test
        @DisplayName("Should create request with null initiatorId")
        void shouldCreateRequestWithNullInitiatorId() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            // When
            TransferRequest request = new TransferRequest(fromId, toId, amount, "USD", null, null, null);

            // Then
            assertThat(request.initiatorId()).isNull();
        }

        @Test
        @DisplayName("Should create request with null initiatorRole")
        void shouldCreateRequestWithNullInitiatorRole() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            // When
            TransferRequest request = new TransferRequest(fromId, toId, amount, "USD", UUID.randomUUID(), null, null);

            // Then
            assertThat(request.initiatorRole()).isNull();
        }

        @Test
        @DisplayName("Should create request with null reason")
        void shouldCreateRequestWithNullReason() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            // When
            TransferRequest request = new TransferRequest(fromId, toId, amount, "USD", UUID.randomUUID(), "TELLER", null);

            // Then
            assertThat(request.reason()).isNull();
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when fromId is null")
        void shouldThrowExceptionWhenFromIdIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new TransferRequest(
                    null, UUID.randomUUID(), new BigDecimal("500.00"), "USD", null, null, null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("fromId must not be null");
        }

        @Test
        @DisplayName("Should throw exception when toId is null")
        void shouldThrowExceptionWhenToIdIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new TransferRequest(
                    UUID.randomUUID(), null, new BigDecimal("500.00"), "USD", null, null, null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("toId must not be null");
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void shouldThrowExceptionWhenAmountIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new TransferRequest(
                    UUID.randomUUID(), UUID.randomUUID(), null, "USD", null, null, null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount must not be null");
        }

        @Test
        @DisplayName("Should throw exception when amount is zero")
        void shouldThrowExceptionWhenAmountIsZero() {
            // Given/When/Then
            assertThatThrownBy(() -> new TransferRequest(
                    UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO, "USD", null, null, null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount must be positive");
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void shouldThrowExceptionWhenAmountIsNegative() {
            // Given/When/Then
            assertThatThrownBy(() -> new TransferRequest(
                    UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("-100.00"), "USD", null, null, null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount must be positive");
        }

        @Test
        @DisplayName("Should set default currency when null")
        void shouldSetDefaultCurrencyWhenNull() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            // When
            TransferRequest request = new TransferRequest(fromId, toId, amount, null, null, null, null);

            // Then
            assertThat(request.currency()).isEqualTo("USD");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle amount with 4 decimal places")
        void shouldHandleAmountWith4DecimalPlaces() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("123.4567");

            // When
            TransferRequest request = new TransferRequest(fromId, toId, amount);

            // Then
            assertThat(request.amount()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle very large amount")
        void shouldHandleVeryLargeAmount() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("10000000.00");

            // When
            TransferRequest request = new TransferRequest(fromId, toId, amount);

            // Then
            assertThat(request.amount()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle different currencies")
        void shouldHandleDifferentCurrencies() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            String[] currencies = {"USD", "EUR", "GBP", "JPY", "CHF"};

            for (String currency : currencies) {
                // When
                TransferRequest request = new TransferRequest(fromId, toId, new BigDecimal("500.00"), currency);

                // Then
                assertThat(request.currency()).isEqualTo(currency);
            }
        }

        @Test
        @DisplayName("Should handle same from and to account")
        void shouldHandleSameFromAndToAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            // When
            TransferRequest request = new TransferRequest(accountId, accountId, amount);

            // Then
            assertThat(request.fromId()).isEqualTo(accountId);
            assertThat(request.toId()).isEqualTo(accountId);
        }

        @Test
        @DisplayName("Should handle long reason")
        void shouldHandleLongReason() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String longReason = "a".repeat(400);

            // When
            TransferRequest request = new TransferRequest(fromId, toId, amount, "USD", null, null, longReason);

            // Then
            assertThat(request.reason()).hasSize(400);
        }

        @Test
        @DisplayName("Should handle empty reason")
        void shouldHandleEmptyReason() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            // When
            TransferRequest request = new TransferRequest(fromId, toId, amount, "USD", null, null, "");

            // Then
            assertThat(request.reason()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should equal another request with same fields")
        void shouldEqualAnotherRequestWithSameFields() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            TransferRequest request1 = new TransferRequest(fromId, toId, amount);
            TransferRequest request2 = new TransferRequest(fromId, toId, amount);

            // Then
            assertThat(request1).isEqualTo(request2);
        }

        @Test
        @DisplayName("Should not equal request with different fromId")
        void shouldNotEqualRequestWithDifferentFromId() {
            // Given
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            TransferRequest request1 = new TransferRequest(UUID.randomUUID(), toId, amount);
            TransferRequest request2 = new TransferRequest(UUID.randomUUID(), toId, amount);

            // Then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            TransferRequest request = new TransferRequest(fromId, toId, amount);

            // Then
            assertThat(request.hashCode()).isNotNull();
            assertThat(request.hashCode()).isEqualTo(request.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString format")
        void shouldHaveCorrectToStringFormat() {
            // Given
            UUID fromId = UUID.randomUUID();
            UUID toId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            TransferRequest request = new TransferRequest(fromId, toId, amount);

            // Then
            assertThat(request.toString()).contains("TransferRequest");
        }
    }
}
