package com.neobank.loans.internal;

import com.neobank.core.accounts.api.AccountApi;
import com.neobank.auth.api.UserStatusChecker;
import com.neobank.loans.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
 * Unit tests for LoanService using JUnit 5 and Mockito.
 * Tests loan application, retrieval, and disbursement operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService Unit Tests")
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private InterestEngine interestEngine;

    @Mock
    private AmortizationSchedule amortizationSchedule;

    @Mock
    private AccountApi accountApi;

    @Mock
    private UserStatusChecker userStatusChecker;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(
                loanRepository, loanMapper, interestEngine,
                amortizationSchedule, accountApi, userStatusChecker
        );
    }

    @Nested
    @DisplayName("Loan Application")
    class LoanApplicationTests {

        @Test
        @DisplayName("Should apply for loan successfully with PENDING status")
        void shouldApplyForLoanSuccessfullyWithPendingStatus() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);
            entity.setAccountId(accountId);
            entity.setPrincipal(principal);
            entity.setStatus(ApplicationStatus.PENDING);

            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));
            BigDecimal interestRate = new BigDecimal("0.07");
            BigDecimal monthlyPayment = new BigDecimal("308.77");
            List<AmortizationEntry> schedule = List.of();

            given(interestEngine.calculateInterestRateFor(riskProfile)).willReturn(interestRate);
            given(interestEngine.calculateMonthlyPayment(principal, interestRate, termMonths)).willReturn(monthlyPayment);
            given(amortizationSchedule.generateSchedule(principal, interestRate, termMonths)).willReturn(schedule);
            given(loanMapper.toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), any(RiskProfile.class)))
                    .willReturn(entity);
            given(loanMapper.serializeSchedule(schedule)).willReturn("[]");
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            LoanApplicationResult result = loanService.apply(request);

            // Then
            assertThat(result.status()).isIn(ApplicationStatus.APPROVED, ApplicationStatus.PENDING);
            assertThat(result.loanId()).isNotNull();
            assertThat(result.calculatedMonthlyPayment()).isEqualByComparingTo(monthlyPayment);
            verify(loanRepository).save(entity);
        }

        @Test
        @DisplayName("Should calculate interest rate using risk profile")
        void shouldCalculateInterestRateUsingRiskProfile() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);

            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));
            BigDecimal interestRate = new BigDecimal("0.07");
            BigDecimal monthlyPayment = new BigDecimal("308.77");
            List<AmortizationEntry> schedule = List.of();

            given(interestEngine.calculateInterestRateFor(riskProfile)).willReturn(interestRate);
            given(interestEngine.calculateMonthlyPayment(principal, interestRate, termMonths)).willReturn(monthlyPayment);
            given(amortizationSchedule.generateSchedule(principal, interestRate, termMonths)).willReturn(schedule);
            given(loanMapper.toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), any(RiskProfile.class)))
                    .willReturn(entity);
            given(loanMapper.serializeSchedule(schedule)).willReturn("[]");
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            loanService.apply(request);

            // Then
            verify(interestEngine).calculateInterestRateFor(riskProfile);
        }

        @Test
        @DisplayName("Should calculate monthly payment using interest engine")
        void shouldCalculateMonthlyPaymentUsingInterestEngine() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);

            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));
            BigDecimal interestRate = new BigDecimal("0.07");
            BigDecimal monthlyPayment = new BigDecimal("308.77");
            List<AmortizationEntry> schedule = List.of();

            given(interestEngine.calculateInterestRateFor(riskProfile)).willReturn(interestRate);
            given(interestEngine.calculateMonthlyPayment(principal, interestRate, termMonths)).willReturn(monthlyPayment);
            given(amortizationSchedule.generateSchedule(principal, interestRate, termMonths)).willReturn(schedule);
            given(loanMapper.toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), any(RiskProfile.class)))
                    .willReturn(entity);
            given(loanMapper.serializeSchedule(schedule)).willReturn("[]");
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            loanService.apply(request);

            // Then
            verify(interestEngine).calculateMonthlyPayment(principal, interestRate, termMonths);
        }

        @Test
        @DisplayName("Should generate amortization schedule")
        void shouldGenerateAmortizationSchedule() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);

            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));
            BigDecimal interestRate = new BigDecimal("0.07");
            BigDecimal monthlyPayment = new BigDecimal("308.77");
            List<AmortizationEntry> schedule = List.of();

            given(interestEngine.calculateInterestRateFor(riskProfile)).willReturn(interestRate);
            given(interestEngine.calculateMonthlyPayment(principal, interestRate, termMonths)).willReturn(monthlyPayment);
            given(amortizationSchedule.generateSchedule(principal, interestRate, termMonths)).willReturn(schedule);
            given(loanMapper.toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), any(RiskProfile.class)))
                    .willReturn(entity);
            given(loanMapper.serializeSchedule(schedule)).willReturn("[]");
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            loanService.apply(request);

            // Then
            verify(amortizationSchedule).generateSchedule(principal, interestRate, termMonths);
            verify(loanMapper).serializeSchedule(schedule);
        }

        @Test
        @DisplayName("Should handle application failure")
        void shouldHandleApplicationFailure() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            given(interestEngine.calculateInterestRateFor(any(RiskProfile.class)))
                    .willThrow(new RuntimeException("Interest calculation failed"));

            // When
            assertThatThrownBy(() -> loanService.apply(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Interest calculation failed");
        }

        @Test
        @DisplayName("Should handle null principal in request")
        void shouldHandleNullPrincipalInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), null, 36, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("principal must be positive");
        }

        @Test
        @DisplayName("Should handle zero principal in request")
        void shouldHandleZeroPrincipalInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), BigDecimal.ZERO, 36, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("principal must be positive");
        }

        @Test
        @DisplayName("Should handle negative principal in request")
        void shouldHandleNegativePrincipalInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("-1000.00"), 36, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("principal must be positive");
        }

        @Test
        @DisplayName("Should handle zero term months in request")
        void shouldHandleZeroTermMonthsInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), 0, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("termMonths must be positive");
        }

        @Test
        @DisplayName("Should handle negative term months in request")
        void shouldHandleNegativeTermMonthsInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), -12, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("termMonths must be positive");
        }

        @Test
        @DisplayName("Should handle null purpose in request")
        void shouldHandleNullPurposeInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), 36, null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("purpose must not be blank");
        }

        @Test
        @DisplayName("Should handle blank purpose in request")
        void shouldHandleBlankPurposeInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    UUID.randomUUID(), new BigDecimal("10000.00"), 36, "   "
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("purpose must not be blank");
        }

        @Test
        @DisplayName("Should handle null account ID in request")
        void shouldHandleNullAccountIdInRequest() {
            // Given/When/Then
            assertThatThrownBy(() -> new LoanApplicationRequest(
                    null, new BigDecimal("10000.00"), 36, "personal"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("accountId must not be null");
        }
    }

    @Nested
    @DisplayName("Loan Retrieval")
    class LoanRetrievalTests {

        @Test
        @DisplayName("Should get loan by ID successfully")
        void shouldGetLoanByIdSuccessfully() {
            // Given
            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);
            entity.setAccountId(UUID.randomUUID());
            entity.setPrincipal(new BigDecimal("10000.00"));
            entity.setStatus(ApplicationStatus.PENDING);

            List<AmortizationEntry> schedule = List.of();
            LoanDetails details = new LoanDetails(
                    loanId, entity.getAccountId(), entity.getPrincipal(),
                    entity.getOutstandingBalance(), entity.getInterestRate(),
                    entity.getTermMonths(), entity.getMonthlyPayment(),
                    entity.getStatus(), entity.getCreatedAt(), entity.getDisbursedAt(),
                    schedule
            );

            given(loanRepository.findById(loanId)).willReturn(Optional.of(entity));
            given(loanMapper.parseSchedule(entity.getAmortizationScheduleJson())).willReturn(schedule);
            given(loanMapper.toDetails(entity, schedule)).willReturn(details);

            // When
            LoanDetails result = loanService.getLoan(loanId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(loanId);
        }

        @Test
        @DisplayName("Should return null when loan not found by ID")
        void shouldReturnNullWhenLoanNotFoundById() {
            // Given
            UUID loanId = UUID.randomUUID();
            given(loanRepository.findById(loanId)).willReturn(Optional.empty());

            // When
            LoanDetails result = loanService.getLoan(loanId);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle empty amortization schedule")
        void shouldHandleEmptyAmortizationSchedule() {
            // Given
            UUID loanId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);
            entity.setAccountId(accountId);
            entity.setAmortizationScheduleJson(null);

            given(loanRepository.findById(loanId)).willReturn(Optional.of(entity));
            given(loanMapper.toDetails(entity, java.util.List.of())).willReturn(new com.neobank.loans.LoanDetails(
                loanId, accountId, new java.math.BigDecimal("10000"), new java.math.BigDecimal("10000"),
                new java.math.BigDecimal("0.05"), 12, new java.math.BigDecimal("1000"),
                com.neobank.loans.ApplicationStatus.PENDING, java.time.Instant.now(), null, java.util.List.of()
            ));

            // When
            com.neobank.loans.LoanDetails result = loanService.getLoan(loanId);

            // Then - Should not throw exception even with empty schedule
            assertThat(result).isNotNull();
            assertThat(result.schedule()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Loan Disbursement")
    class LoanDisbursementTests {

        @Test
        @DisplayName("Should disburse loan successfully from PENDING status")
        void shouldDisburseLoanSuccessfullyFromPendingStatus() {
            // Given
            UUID loanId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            Instant disbursedAt = Instant.now();

            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);
            entity.setAccountId(accountId);
            entity.setPrincipal(principal);
            entity.setStatus(ApplicationStatus.PENDING);

            given(loanRepository.findById(loanId)).willReturn(Optional.of(entity));
            given(accountApi.creditAccount(accountId, principal)).willReturn(new com.neobank.core.accounts.Account(accountId, "Test User", principal));
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            DisbursementResult result = loanService.disburse(loanId);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.loanId()).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(ApplicationStatus.DISBURSED);
            assertThat(entity.getDisbursedAt()).isNotNull();
            verify(accountApi).creditAccount(accountId, principal);
            verify(loanRepository).save(entity);
        }

        @Test
        @DisplayName("Should disburse loan successfully from APPROVED status")
        void shouldDisburseLoanSuccessfullyFromApprovedStatus() {
            // Given
            UUID loanId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");

            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);
            entity.setAccountId(accountId);
            entity.setPrincipal(principal);
            entity.setStatus(ApplicationStatus.APPROVED);

            given(loanRepository.findById(loanId)).willReturn(Optional.of(entity));
            given(accountApi.creditAccount(accountId, principal)).willReturn(new com.neobank.core.accounts.Account(accountId, "Test User", principal));
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            DisbursementResult result = loanService.disburse(loanId);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(entity.getStatus()).isEqualTo(ApplicationStatus.DISBURSED);
        }

        @Test
        @DisplayName("Should fail disbursement when loan not in PENDING or APPROVED status")
        void shouldFailDisbursementWhenLoanNotInPendingOrApprovedStatus() {
            // Given
            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);
            entity.setStatus(ApplicationStatus.DISBURSED);

            given(loanRepository.findById(loanId)).willReturn(Optional.of(entity));

            // When
            DisbursementResult result = loanService.disburse(loanId);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("PENDING or APPROVED");
            verify(accountApi, never()).creditAccount(any(UUID.class), any(BigDecimal.class));
        }

        @Test
        @DisplayName("Should fail disbursement when loan not found")
        void shouldFailDisbursementWhenLoanNotFound() {
            // Given
            UUID loanId = UUID.randomUUID();
            given(loanRepository.findById(loanId)).willReturn(Optional.empty());

            // When
            DisbursementResult result = loanService.disburse(loanId);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Loan not found");
        }

        @Test
        @DisplayName("Should handle account API exception during disbursement")
        void shouldHandleAccountApiExceptionDuringDisbursement() {
            // Given
            UUID loanId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");

            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);
            entity.setAccountId(accountId);
            entity.setPrincipal(principal);
            entity.setStatus(ApplicationStatus.PENDING);

            given(loanRepository.findById(loanId)).willReturn(Optional.of(entity));
            given(accountApi.creditAccount(accountId, principal))
                    .willThrow(new RuntimeException("Account API error"));

            // When
            DisbursementResult result = loanService.disburse(loanId);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Account API error");
            verify(loanRepository, never()).save(entity);
        }

        @Test
        @DisplayName("Should handle null account API response")
        void shouldHandleNullAccountApiResponse() {
            // Given
            UUID loanId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");

            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);
            entity.setAccountId(accountId);
            entity.setPrincipal(principal);
            entity.setStatus(ApplicationStatus.PENDING);

            given(loanRepository.findById(loanId)).willReturn(Optional.of(entity));
            // Simulate null/failed account API response
            given(accountApi.creditAccount(accountId, principal)).willThrow(new RuntimeException("Account API unavailable"));

            // When
            DisbursementResult result = loanService.disburse(loanId);

            // Then
            assertThat(result.success()).isFalse();
        }
    }

    @Nested
    @DisplayName("Internal Methods")
    class InternalMethodsTests {

        @Test
        @DisplayName("Should approve loan when in PENDING status")
        void shouldApproveLoanWhenInPendingStatus() {
            // Given
            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);
            entity.setStatus(ApplicationStatus.PENDING);

            given(loanRepository.findById(loanId)).willReturn(Optional.of(entity));
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            loanService.approveLoan(loanId);

            // Then
            assertThat(entity.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
            verify(loanRepository).save(entity);
        }

        @Test
        @DisplayName("Should not approve loan when not in PENDING status")
        void shouldNotApproveLoanWhenNotInPendingStatus() {
            // Given
            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);
            entity.setStatus(ApplicationStatus.APPROVED);

            given(loanRepository.findById(loanId)).willReturn(Optional.of(entity));

            // When
            loanService.approveLoan(loanId);

            // Then
            assertThat(entity.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
            verify(loanRepository, never()).save(entity);
        }

        @Test
        @DisplayName("Should not approve loan when loan not found")
        void shouldNotApproveLoanWhenLoanNotFound() {
            // Given
            UUID loanId = UUID.randomUUID();
            given(loanRepository.findById(loanId)).willReturn(Optional.empty());

            // When
            loanService.approveLoan(loanId);

            // Then
            verify(loanRepository, never()).save(any(LoanEntity.class));
        }

        @Test
        @DisplayName("Should create risk profile with default values")
        void shouldCreateRiskProfileWithDefaultValues() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.00");
            int termMonths = 36;
            String purpose = "personal";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);

            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));
            BigDecimal interestRate = new BigDecimal("0.07");
            BigDecimal monthlyPayment = new BigDecimal("308.77");
            List<AmortizationEntry> schedule = List.of();

            given(interestEngine.calculateInterestRateFor(riskProfile)).willReturn(interestRate);
            given(interestEngine.calculateMonthlyPayment(principal, interestRate, termMonths)).willReturn(monthlyPayment);
            given(amortizationSchedule.generateSchedule(principal, interestRate, termMonths)).willReturn(schedule);
            given(loanMapper.toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), any(RiskProfile.class)))
                    .willReturn(entity);
            given(loanMapper.serializeSchedule(schedule)).willReturn("[]");
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            loanService.apply(request);

            // Then - Risk profile should be created with default values
            ArgumentCaptor<RiskProfile> riskProfileCaptor = ArgumentCaptor.forClass(RiskProfile.class);
            verify(loanMapper).toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), riskProfileCaptor.capture());

            RiskProfile capturedProfile = riskProfileCaptor.getValue();
            assertThat(capturedProfile.creditScore()).isEqualTo(720);
            assertThat(capturedProfile.debtToIncomeRatio()).isEqualByComparingTo(new BigDecimal("0.35"));
            assertThat(capturedProfile.employmentYears()).isEqualTo(5);
            assertThat(capturedProfile.annualIncome()).isEqualByComparingTo(new BigDecimal("75000"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large principal amount")
        void shouldHandleVeryLargePrincipalAmount() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("1000000.00");
            int termMonths = 360;
            String purpose = "home";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);

            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));
            BigDecimal interestRate = new BigDecimal("0.07");
            BigDecimal monthlyPayment = new BigDecimal("6653.02");
            List<AmortizationEntry> schedule = List.of();

            given(interestEngine.calculateInterestRateFor(riskProfile)).willReturn(interestRate);
            given(interestEngine.calculateMonthlyPayment(principal, interestRate, termMonths)).willReturn(monthlyPayment);
            given(amortizationSchedule.generateSchedule(principal, interestRate, termMonths)).willReturn(schedule);
            given(loanMapper.toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), any(RiskProfile.class)))
                    .willReturn(entity);
            given(loanMapper.serializeSchedule(schedule)).willReturn("[]");
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            LoanApplicationResult result = loanService.apply(request);

            // Then
            assertThat(result.status()).isIn(ApplicationStatus.APPROVED, ApplicationStatus.PENDING);
        }

        @Test
        @DisplayName("Should handle very short term")
        void shouldHandleVeryShortTerm() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("1000.00");
            int termMonths = 3;
            String purpose = "personal";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);

            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));
            BigDecimal interestRate = new BigDecimal("0.07");
            BigDecimal monthlyPayment = new BigDecimal("337.39");
            List<AmortizationEntry> schedule = List.of();

            given(interestEngine.calculateInterestRateFor(riskProfile)).willReturn(interestRate);
            given(interestEngine.calculateMonthlyPayment(principal, interestRate, termMonths)).willReturn(monthlyPayment);
            given(amortizationSchedule.generateSchedule(principal, interestRate, termMonths)).willReturn(schedule);
            given(loanMapper.toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), any(RiskProfile.class)))
                    .willReturn(entity);
            given(loanMapper.serializeSchedule(schedule)).willReturn("[]");
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            LoanApplicationResult result = loanService.apply(request);

            // Then
            assertThat(result.status()).isIn(ApplicationStatus.APPROVED, ApplicationStatus.PENDING);
        }

        @Test
        @DisplayName("Should handle very long term")
        void shouldHandleVeryLongTerm() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("500000.00");
            int termMonths = 360;
            String purpose = "home";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);

            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));
            BigDecimal interestRate = new BigDecimal("0.07");
            BigDecimal monthlyPayment = new BigDecimal("3326.51");
            List<AmortizationEntry> schedule = List.of();

            given(interestEngine.calculateInterestRateFor(riskProfile)).willReturn(interestRate);
            given(interestEngine.calculateMonthlyPayment(principal, interestRate, termMonths)).willReturn(monthlyPayment);
            given(amortizationSchedule.generateSchedule(principal, interestRate, termMonths)).willReturn(schedule);
            given(loanMapper.toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), any(RiskProfile.class)))
                    .willReturn(entity);
            given(loanMapper.serializeSchedule(schedule)).willReturn("[]");
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            LoanApplicationResult result = loanService.apply(request);

            // Then
            assertThat(result.status()).isIn(ApplicationStatus.APPROVED, ApplicationStatus.PENDING);
        }

        @Test
        @DisplayName("Should handle principal with 2 decimal places")
        void shouldHandlePrincipalWith2DecimalPlaces() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("10000.99");
            int termMonths = 36;
            String purpose = "personal";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            UUID loanId = UUID.randomUUID();
            LoanEntity entity = new LoanEntity();
            entity.setId(loanId);

            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));
            BigDecimal interestRate = new BigDecimal("0.07");
            BigDecimal monthlyPayment = new BigDecimal("308.80");
            List<AmortizationEntry> schedule = List.of();

            given(interestEngine.calculateInterestRateFor(riskProfile)).willReturn(interestRate);
            given(interestEngine.calculateMonthlyPayment(principal, interestRate, termMonths)).willReturn(monthlyPayment);
            given(amortizationSchedule.generateSchedule(principal, interestRate, termMonths)).willReturn(schedule);
            given(loanMapper.toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), any(RiskProfile.class)))
                    .willReturn(entity);
            given(loanMapper.serializeSchedule(schedule)).willReturn("[]");
            given(loanRepository.save(entity)).willReturn(entity);

            // When
            LoanApplicationResult result = loanService.apply(request);

            // Then
            assertThat(result.status()).isIn(ApplicationStatus.APPROVED, ApplicationStatus.PENDING);
        }

        @Test
        @DisplayName("Should handle multiple loan applications in sequence")
        void shouldHandleMultipleLoanApplicationsInSequence() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal principal = new BigDecimal("5000.00");
            int termMonths = 24;
            String purpose = "auto";
            LoanApplicationRequest request = new LoanApplicationRequest(accountId, principal, termMonths, purpose);

            UUID loanId1 = UUID.randomUUID();
            UUID loanId2 = UUID.randomUUID();
            LoanEntity entity1 = new LoanEntity();
            entity1.setId(loanId1);
            LoanEntity entity2 = new LoanEntity();
            entity2.setId(loanId2);

            RiskProfile riskProfile = new RiskProfile(720, new BigDecimal("0.35"), 5, new BigDecimal("75000"));
            BigDecimal interestRate = new BigDecimal("0.07");
            BigDecimal monthlyPayment = new BigDecimal("223.05");
            List<AmortizationEntry> schedule = List.of();

            given(interestEngine.calculateInterestRateFor(riskProfile)).willReturn(interestRate);
            given(interestEngine.calculateMonthlyPayment(principal, interestRate, termMonths)).willReturn(monthlyPayment);
            given(amortizationSchedule.generateSchedule(principal, interestRate, termMonths)).willReturn(schedule);
            given(loanMapper.toEntity(any(UUID.class), any(UUID.class), any(BigDecimal.class), anyInt(), any(RiskProfile.class)))
                    .willReturn(entity1, entity2);
            given(loanMapper.serializeSchedule(schedule)).willReturn("[]");
            given(loanRepository.save(entity1)).willReturn(entity1);
            given(loanRepository.save(entity2)).willReturn(entity2);

            // When
            LoanApplicationResult result1 = loanService.apply(request);
            LoanApplicationResult result2 = loanService.apply(request);

            // Then
            assertThat(result1.loanId()).isNotNull();
            assertThat(result2.loanId()).isNotNull();
            assertThat(result1.loanId()).isNotEqualTo(result2.loanId());
        }
    }
}
