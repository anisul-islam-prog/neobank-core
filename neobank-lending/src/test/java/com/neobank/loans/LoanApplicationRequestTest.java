package com.neobank.loans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for LoanApplicationRequest record using JUnit 5.
 * Tests record construction and validation.
 */
@DisplayName("LoanApplicationRequest Unit Tests")
class LoanApplicationRequestTest {

    @Nested
    @DisplayName("Record Construction")
    class RecordConstructionTests {

        @Test
        @DisplayName("Should create request with all fields")
        void shouldCreateRequestWithAllFields() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            // Then
            assertThat(request.accountId()).isEqualTo(accountId);
            assertThat(request.principal()).isEqualByComparingTo(principal);
            assertThat(request.termMonths()).isEqualTo(termMonths);
            assertThat(request.purpose()).isEqualTo(purpose);
        }

        @Test
        @DisplayName("Should create request with auto purpose")
        void shouldCreateRequestWithAutoPurpose() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("25000.00");
            int termMonths = 60;
            String purpose = "auto";

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            // Then
            assertThat(request.purpose()).isEqualTo("auto");
        }

        @Test
        @DisplayName("Should create request with home purpose")
        void shouldCreateRequestWithHomePurpose() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("250000.00");
            int termMonths = 360;
            String purpose = "home";

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            // Then
            assertThat(request.purpose()).isEqualTo("home");
        }

        @Test
        @DisplayName("Should create request with business purpose")
        void shouldCreateRequestWithBusinessPurpose() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("50000.00");
            int termMonths = 48;
            String purpose = "business";

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            // Then
            assertThat(request.purpose()).isEqualTo("business");
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when account ID is null")
        void shouldThrowExceptionWhenAccountIdIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    null, new BigDecimal("10000.00"), 36, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("accountId must not be null");
        }

        @Test
        @DisplayName("Should throw exception when principal is null")
        void shouldThrowExceptionWhenPrincipalIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), null, 36, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("principal must be positive");
        }

        @Test
        @DisplayName("Should throw exception when principal is zero")
        void shouldThrowExceptionWhenPrincipalIsZero() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), BigDecimal.ZERO, 36, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("principal must be positive");
        }

        @Test
        @DisplayName("Should throw exception when principal is negative")
        void shouldThrowExceptionWhenPrincipalIsNegative() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("-1000.00"), 36, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("principal must be positive");
        }

        @Test
        @DisplayName("Should throw exception when term months is zero")
        void shouldThrowExceptionWhenTermMonthsIsZero() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), 0, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("termMonths must be positive");
        }

        @Test
        @DisplayName("Should throw exception when term months is negative")
        void shouldThrowExceptionWhenTermMonthsIsNegative() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), -12, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("termMonths must be positive");
        }

        @Test
        @DisplayName("Should throw exception when purpose is null")
        void shouldThrowExceptionWhenPurposeIsNull() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), 36, null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("purpose must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when purpose is blank")
        void shouldThrowExceptionWhenPurposeIsBlank() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), 36, "   "
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("purpose must not be blank");
        }

        @Test
        @DisplayName("Should throw exception when purpose is empty")
        void shouldThrowExceptionWhenPurposeIsEmpty() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), 36, ""
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("purpose must not be blank");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very small principal")
        void shouldHandleVerySmallPrincipal() {
            // Given
            BigDecimal principal = new BigDecimal("100.00");

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(
                    UUID.randomUUID(), principal, 12, "personal"
            );

            // Then
            assertThat(request.principal()).isEqualByComparingTo(principal);
        }

        @Test
        @DisplayName("Should handle very large principal")
        void shouldHandleVeryLargePrincipal() {
            // Given
            BigDecimal principal = new BigDecimal("1000000.00");

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(
                    UUID.randomUUID(), principal, 360, "home"
            );

            // Then
            assertThat(request.principal()).isEqualByComparingTo(principal);
        }

        @Test
        @DisplayName("Should handle principal with 2 decimal places")
        void shouldHandlePrincipalWith2DecimalPlaces() {
            // Given
            BigDecimal principal = new BigDecimal("10000.99");

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(
                    UUID.randomUUID(), principal, 36, "personal"
            );

            // Then
            assertThat(request.principal()).isEqualByComparingTo(principal);
        }

        @Test
        @DisplayName("Should handle single month term")
        void shouldHandleSingleMonthTerm() {
            // Given
            int termMonths = 1;

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("1000.00"), termMonths, "personal"
            );

            // Then
            assertThat(request.termMonths()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle very long term")
        void shouldHandleVeryLongTerm() {
            // Given
            int termMonths = 480;

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("200000.00"), termMonths, "home"
            );

            // Then
            assertThat(request.termMonths()).isEqualTo(480);
        }

        @Test
        @DisplayName("Should handle long purpose string")
        void shouldHandleLongPurposeString() {
            // Given
            String purpose = "a".repeat(100);

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), 36, purpose
            );

            // Then
            assertThat(request.purpose()).hasSize(100);
        }

        @Test
        @DisplayName("Should handle purpose with special characters")
        void shouldHandlePurposeWithSpecialCharacters() {
            // Given
            String purpose = "personal_loan_2024";

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), 36, purpose
            );

            // Then
            assertThat(request.purpose()).isEqualTo(purpose);
        }

        @Test
        @DisplayName("Should handle purpose with unicode characters")
        void shouldHandlePurposeWithUnicodeCharacters() {
            // Given
            String purpose = "个人贷款";

            // When
            LoanApplicationRequest request = new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), 36, purpose
            );

            // Then
            assertThat(request.purpose()).isEqualTo(purpose);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should equal another request with same fields")
        void shouldEqualAnotherRequestWithSameFields() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";

            LoanApplicationRequest request1 = new LoanApplicationRequest(accountId, principal, termMonths, purpose);
            LoanApplicationRequest request2 = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            // Then
            assertThat(request1).isEqualTo(request2);
        }

        @Test
        @DisplayName("Should not equal request with different account ID")
        void shouldNotEqualRequestWithDifferentAccountId() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";

            LoanApplicationRequest request1 = new LoanApplicationRequest(
                    UUID.randomUUID(), principal, termMonths, purpose
            );
            LoanApplicationRequest request2 = new LoanApplicationRequest(
                    UUID.randomUUID(), principal, termMonths, purpose
            );

            // Then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should have consistent hash code")
        void shouldHaveConsistentHashCode() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";

            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

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
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";

            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            // Then
            assertThat(request.toString()).contains("LoanApplicationRequest");
            assertThat(request.toString()).contains(purpose);
        }
    }
}
