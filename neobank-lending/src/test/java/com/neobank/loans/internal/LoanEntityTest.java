package com.neobank.loans.internal;

import com.neobank.loans.ApplicationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LoanEntity using JUnit 5.
 * Tests entity state and field accessors.
 */
@DisplayName("LoanEntity Unit Tests")
class LoanEntityTest {

    private LoanEntity loanEntity;

    @BeforeEach
    void setUp() {
        loanEntity = new LoanEntity();
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("Should create entity with default constructor")
        void shouldCreateEntityWithDefaultConstructor() {
            // When
            LoanEntity entity = new LoanEntity();

            // Then
            assertThat(entity).isNotNull();
        }

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            loanEntity.setId(id);

            // Then
            assertThat(loanEntity.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get account ID")
        void shouldSetAndGetAccountId() {
            // Given
            UUID accountId = UUID.randomUUID();

            // When
            loanEntity.setAccountId(accountId);

            // Then
            assertThat(loanEntity.getAccountId()).isEqualTo(accountId);
        }

        @Test
        @DisplayName("Should set and get principal")
        void shouldSetAndGetPrincipal() {
            // Given
            BigDecimal principal = new BigDecimal("10000.00");

            // When
            loanEntity.setPrincipal(principal);

            // Then
            assertThat(loanEntity.getPrincipal()).isEqualByComparingTo(principal);
        }

        @Test
        @DisplayName("Should set and get outstanding balance")
        void shouldSetAndGetOutstandingBalance() {
            // Given
            BigDecimal outstandingBalance = new BigDecimal("9500.00");

            // When
            loanEntity.setOutstandingBalance(outstandingBalance);

            // Then
            assertThat(loanEntity.getOutstandingBalance()).isEqualByComparingTo(outstandingBalance);
        }

        @Test
        @DisplayName("Should set and get interest rate")
        void shouldSetAndGetInterestRate() {
            // Given
            BigDecimal interestRate = new BigDecimal("0.07");

            // When
            loanEntity.setInterestRate(interestRate);

            // Then
            assertThat(loanEntity.getInterestRate()).isEqualByComparingTo(interestRate);
        }

        @Test
        @DisplayName("Should set and get term months")
        void shouldSetAndGetTermMonths() {
            // Given
            int termMonths = 36;

            // When
            loanEntity.setTermMonths(termMonths);

            // Then
            assertThat(loanEntity.getTermMonths()).isEqualTo(termMonths);
        }

        @Test
        @DisplayName("Should set and get monthly payment")
        void shouldSetAndGetMonthlyPayment() {
            // Given
            BigDecimal monthlyPayment = new BigDecimal("308.77");

            // When
            loanEntity.setMonthlyPayment(monthlyPayment);

            // Then
            assertThat(loanEntity.getMonthlyPayment()).isEqualByComparingTo(monthlyPayment);
        }

        @Test
        @DisplayName("Should set and get status")
        void shouldSetAndGetStatus() {
            // Given
            ApplicationStatus status = ApplicationStatus.PENDING;

            // When
            loanEntity.setStatus(status);

            // Then
            assertThat(loanEntity.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should set and get created at timestamp")
        void shouldSetAndGetCreatedAtTimestamp() {
            // Given
            Instant createdAt = Instant.now();

            // When
            loanEntity.setCreatedAt(createdAt);

            // Then
            assertThat(loanEntity.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should set and get disbursed at timestamp")
        void shouldSetAndGetDisbursedAtTimestamp() {
            // Given
            Instant disbursedAt = Instant.now();

            // When
            loanEntity.setDisbursedAt(disbursedAt);

            // Then
            assertThat(loanEntity.getDisbursedAt()).isEqualTo(disbursedAt);
        }

        @Test
        @DisplayName("Should set and get risk profile JSON")
        void shouldSetAndGetRiskProfileJson() {
            // Given
            String riskProfileJson = "{\"creditScore\":720,\"debtToIncome\":0.35}";

            // When
            loanEntity.setRiskProfileJson(riskProfileJson);

            // Then
            assertThat(loanEntity.getRiskProfileJson()).isEqualTo(riskProfileJson);
        }

        @Test
        @DisplayName("Should set and get amortization schedule JSON")
        void shouldSetAndGetAmortizationScheduleJson() {
            // Given
            String amortizationScheduleJson = "[{\"paymentNumber\":1,\"paymentAmount\":308.77}]";

            // When
            loanEntity.setAmortizationScheduleJson(amortizationScheduleJson);

            // Then
            assertThat(loanEntity.getAmortizationScheduleJson()).isEqualTo(amortizationScheduleJson);
        }
    }

    @Nested
    @DisplayName("ApplicationStatus Enum")
    class ApplicationStatusEnumTests {

        @Test
        @DisplayName("Should have PENDING status")
        void shouldHavePendingStatus() {
            // Then
            assertThat(ApplicationStatus.PENDING).isNotNull();
        }

        @Test
        @DisplayName("Should have APPROVED status")
        void shouldHaveApprovedStatus() {
            // Then
            assertThat(ApplicationStatus.APPROVED).isNotNull();
        }

        @Test
        @DisplayName("Should have REJECTED status")
        void shouldHaveRejectedStatus() {
            // Then
            assertThat(ApplicationStatus.REJECTED).isNotNull();
        }

        @Test
        @DisplayName("Should have DISBURSED status")
        void shouldHaveDisbursedStatus() {
            // Then
            assertThat(ApplicationStatus.DISBURSED).isNotNull();
        }

        @Test
        @DisplayName("Should have ACTIVE status")
        void shouldHaveActiveStatus() {
            // Then
            assertThat(ApplicationStatus.ACTIVE).isNotNull();
        }

        @Test
        @DisplayName("Should have PAID_OFF status")
        void shouldHavePaidOffStatus() {
            // Then
            assertThat(ApplicationStatus.PAID_OFF).isNotNull();
        }

        @Test
        @DisplayName("Should have DEFAULT status")
        void shouldHaveDefaultStatus() {
            // Then
            assertThat(ApplicationStatus.DEFAULT).isNotNull();
        }

        @Test
        @DisplayName("Should have exactly 7 status values")
        void shouldHaveExactly7StatusValues() {
            // Then
            assertThat(ApplicationStatus.values()).hasSize(7);
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionsTests {

        @Test
        @DisplayName("Should transition from PENDING to APPROVED")
        void shouldTransitionFromPendingToApproved() {
            // Given
            loanEntity.setStatus(ApplicationStatus.PENDING);

            // When
            loanEntity.setStatus(ApplicationStatus.APPROVED);

            // Then
            assertThat(loanEntity.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        }

        @Test
        @DisplayName("Should transition from APPROVED to DISBURSED")
        void shouldTransitionFromApprovedToDisbursed() {
            // Given
            loanEntity.setStatus(ApplicationStatus.APPROVED);

            // When
            loanEntity.setStatus(ApplicationStatus.DISBURSED);

            // Then
            assertThat(loanEntity.getStatus()).isEqualTo(ApplicationStatus.DISBURSED);
        }

        @Test
        @DisplayName("Should transition from DISBURSED to ACTIVE")
        void shouldTransitionFromDisbursedToActive() {
            // Given
            loanEntity.setStatus(ApplicationStatus.DISBURSED);

            // When
            loanEntity.setStatus(ApplicationStatus.ACTIVE);

            // Then
            assertThat(loanEntity.getStatus()).isEqualTo(ApplicationStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should transition from ACTIVE to PAID_OFF")
        void shouldTransitionFromActiveToPaidOff() {
            // Given
            loanEntity.setStatus(ApplicationStatus.ACTIVE);

            // When
            loanEntity.setStatus(ApplicationStatus.PAID_OFF);

            // Then
            assertThat(loanEntity.getStatus()).isEqualTo(ApplicationStatus.PAID_OFF);
        }

        @Test
        @DisplayName("Should transition from ACTIVE to DEFAULT")
        void shouldTransitionFromActiveToDefault() {
            // Given
            loanEntity.setStatus(ApplicationStatus.ACTIVE);

            // When
            loanEntity.setStatus(ApplicationStatus.DEFAULT);

            // Then
            assertThat(loanEntity.getStatus()).isEqualTo(ApplicationStatus.DEFAULT);
        }

        @Test
        @DisplayName("Should transition from PENDING to REJECTED")
        void shouldTransitionFromPendingToRejected() {
            // Given
            loanEntity.setStatus(ApplicationStatus.PENDING);

            // When
            loanEntity.setStatus(ApplicationStatus.REJECTED);

            // Then
            assertThat(loanEntity.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // When
            loanEntity.setId(null);

            // Then
            assertThat(loanEntity.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null account ID")
        void shouldHandleNullAccountId() {
            // When
            loanEntity.setAccountId(null);

            // Then
            assertThat(loanEntity.getAccountId()).isNull();
        }

        @Test
        @DisplayName("Should handle null principal")
        void shouldHandleNullPrincipal() {
            // When
            loanEntity.setPrincipal(null);

            // Then
            assertThat(loanEntity.getPrincipal()).isNull();
        }

        @Test
        @DisplayName("Should handle null outstanding balance")
        void shouldHandleNullOutstandingBalance() {
            // When
            loanEntity.setOutstandingBalance(null);

            // Then
            assertThat(loanEntity.getOutstandingBalance()).isNull();
        }

        @Test
        @DisplayName("Should handle null interest rate")
        void shouldHandleNullInterestRate() {
            // When
            loanEntity.setInterestRate(null);

            // Then
            assertThat(loanEntity.getInterestRate()).isNull();
        }

        @Test
        @DisplayName("Should handle null monthly payment")
        void shouldHandleNullMonthlyPayment() {
            // When
            loanEntity.setMonthlyPayment(null);

            // Then
            assertThat(loanEntity.getMonthlyPayment()).isNull();
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // When
            loanEntity.setStatus(null);

            // Then
            assertThat(loanEntity.getStatus()).isNull();
        }

        @Test
        @DisplayName("Should handle null created at timestamp")
        void shouldHandleNullCreatedAtTimestamp() {
            // When
            loanEntity.setCreatedAt(null);

            // Then
            assertThat(loanEntity.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null disbursed at timestamp")
        void shouldHandleNullDisbursedAtTimestamp() {
            // When
            loanEntity.setDisbursedAt(null);

            // Then
            assertThat(loanEntity.getDisbursedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null risk profile JSON")
        void shouldHandleNullRiskProfileJson() {
            // When
            loanEntity.setRiskProfileJson(null);

            // Then
            assertThat(loanEntity.getRiskProfileJson()).isNull();
        }

        @Test
        @DisplayName("Should handle null amortization schedule JSON")
        void shouldHandleNullAmortizationScheduleJson() {
            // When
            loanEntity.setAmortizationScheduleJson(null);

            // Then
            assertThat(loanEntity.getAmortizationScheduleJson()).isNull();
        }

        @Test
        @DisplayName("Should handle empty risk profile JSON")
        void shouldHandleEmptyRiskProfileJson() {
            // When
            loanEntity.setRiskProfileJson("{}");

            // Then
            assertThat(loanEntity.getRiskProfileJson()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle empty amortization schedule JSON")
        void shouldHandleEmptyAmortizationScheduleJson() {
            // When
            loanEntity.setAmortizationScheduleJson("[]");

            // Then
            assertThat(loanEntity.getAmortizationScheduleJson()).isEqualTo("[]");
        }

        @Test
        @DisplayName("Should handle zero principal")
        void shouldHandleZeroPrincipal() {
            // When
            loanEntity.setPrincipal(BigDecimal.ZERO);

            // Then
            assertThat(loanEntity.getPrincipal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle zero outstanding balance")
        void shouldHandleZeroOutstandingBalance() {
            // When
            loanEntity.setOutstandingBalance(BigDecimal.ZERO);

            // Then
            assertThat(loanEntity.getOutstandingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle zero interest rate")
        void shouldHandleZeroInterestRate() {
            // When
            loanEntity.setInterestRate(BigDecimal.ZERO);

            // Then
            assertThat(loanEntity.getInterestRate()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle very large principal")
        void shouldHandleVeryLargePrincipal() {
            // When
            loanEntity.setPrincipal(new BigDecimal("1000000.00"));

            // Then
            assertThat(loanEntity.getPrincipal()).isEqualByComparingTo(new BigDecimal("1000000.00"));
        }

        @Test
        @DisplayName("Should handle principal with 2 decimal places")
        void shouldHandlePrincipalWith2DecimalPlaces() {
            // When
            loanEntity.setPrincipal(new BigDecimal("10000.99"));

            // Then
            assertThat(loanEntity.getPrincipal()).isEqualByComparingTo(new BigDecimal("10000.99"));
        }

        @Test
        @DisplayName("Should handle interest rate with 4 decimal places")
        void shouldHandleInterestRateWith4DecimalPlaces() {
            // When
            loanEntity.setInterestRate(new BigDecimal("0.0725"));

            // Then
            assertThat(loanEntity.getInterestRate()).isEqualByComparingTo(new BigDecimal("0.0725"));
        }

        @Test
        @DisplayName("Should handle long risk profile JSON")
        void shouldHandleLongRiskProfileJson() {
            // Given
            String longJson = "{\"creditScore\":720,\"debtToIncome\":0.35,\"employmentYears\":5,\"annualIncome\":75000}";

            // When
            loanEntity.setRiskProfileJson(longJson);

            // Then
            assertThat(loanEntity.getRiskProfileJson()).isEqualTo(longJson);
        }

        @Test
        @DisplayName("Should handle long amortization schedule JSON")
        void shouldHandleLongAmortizationScheduleJson() {
            // Given
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < 360; i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"paymentNumber\":").append(i + 1).append("}");
            }
            sb.append("]");
            String longJson = sb.toString();

            // When
            loanEntity.setAmortizationScheduleJson(longJson);

            // Then
            assertThat(loanEntity.getAmortizationScheduleJson()).isEqualTo(longJson);
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
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            BigDecimal outstandingBalance = new BigDecimal("9500.00");
            BigDecimal interestRate = new BigDecimal("0.07");
            int termMonths = 36;
            BigDecimal monthlyPayment = new BigDecimal("308.77");
            ApplicationStatus status = ApplicationStatus.ACTIVE;
            Instant createdAt = Instant.now();
            Instant disbursedAt = Instant.now();
            String riskProfileJson = "{\"creditScore\":720}";
            String amortizationScheduleJson = "[{\"paymentNumber\":1}]";

            // When
            loanEntity.setId(id);
            loanEntity.setAccountId(accountId);
            loanEntity.setPrincipal(principal);
            loanEntity.setOutstandingBalance(outstandingBalance);
            loanEntity.setInterestRate(interestRate);
            loanEntity.setTermMonths(termMonths);
            loanEntity.setMonthlyPayment(monthlyPayment);
            loanEntity.setStatus(status);
            loanEntity.setCreatedAt(createdAt);
            loanEntity.setDisbursedAt(disbursedAt);
            loanEntity.setRiskProfileJson(riskProfileJson);
            loanEntity.setAmortizationScheduleJson(amortizationScheduleJson);

            // Then
            assertThat(loanEntity.getId()).isEqualTo(id);
            assertThat(loanEntity.getAccountId()).isEqualTo(accountId);
            assertThat(loanEntity.getPrincipal()).isEqualByComparingTo(principal);
            assertThat(loanEntity.getOutstandingBalance()).isEqualByComparingTo(outstandingBalance);
            assertThat(loanEntity.getInterestRate()).isEqualByComparingTo(interestRate);
            assertThat(loanEntity.getTermMonths()).isEqualTo(termMonths);
            assertThat(loanEntity.getMonthlyPayment()).isEqualByComparingTo(monthlyPayment);
            assertThat(loanEntity.getStatus()).isEqualTo(status);
            assertThat(loanEntity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(loanEntity.getDisbursedAt()).isEqualTo(disbursedAt);
            assertThat(loanEntity.getRiskProfileJson()).isEqualTo(riskProfileJson);
            assertThat(loanEntity.getAmortizationScheduleJson()).isEqualTo(amortizationScheduleJson);
        }

        @Test
        @DisplayName("Should handle entity with minimal fields")
        void shouldHandleEntityWithMinimalFields() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");

            // When
            loanEntity.setId(id);
            loanEntity.setAccountId(accountId);
            loanEntity.setPrincipal(principal);

            // Then
            assertThat(loanEntity.getId()).isEqualTo(id);
            assertThat(loanEntity.getAccountId()).isEqualTo(accountId);
            assertThat(loanEntity.getPrincipal()).isEqualByComparingTo(principal);
            assertThat(loanEntity.getOutstandingBalance()).isNull();
            assertThat(loanEntity.getStatus()).isNull();
        }

        @Test
        @DisplayName("Should handle entity with PENDING status")
        void shouldHandleEntityWithPendingStatus() {
            // Given
            loanEntity.setId(UUID.randomUUID());
            loanEntity.setAccountId(UUID.randomUUID());
            loanEntity.setPrincipal(new BigDecimal("10000.00"));
            loanEntity.setStatus(ApplicationStatus.PENDING);

            // Then
            assertThat(loanEntity.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        }

        @Test
        @DisplayName("Should handle entity with DISBURSED status and disbursed at")
        void shouldHandleEntityWithDisbursedStatusAndDisbursedAt() {
            // Given
            Instant disbursedAt = Instant.now();
            loanEntity.setId(UUID.randomUUID());
            loanEntity.setAccountId(UUID.randomUUID());
            loanEntity.setPrincipal(new BigDecimal("10000.00"));
            loanEntity.setStatus(ApplicationStatus.DISBURSED);
            loanEntity.setDisbursedAt(disbursedAt);

            // Then
            assertThat(loanEntity.getStatus()).isEqualTo(ApplicationStatus.DISBURSED);
            assertThat(loanEntity.getDisbursedAt()).isEqualTo(disbursedAt);
        }
    }
}
