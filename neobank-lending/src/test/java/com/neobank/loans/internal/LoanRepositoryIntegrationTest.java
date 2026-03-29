package com.neobank.loans.internal;

import com.neobank.loans.ApplicationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for LoanRepository using Testcontainers.
 * Tests repository queries against a real PostgreSQL database.
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("LoanRepository Integration Tests")
class LoanRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private LoanRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("Save and Retrieve")
    class SaveAndRetrieveTests {

        @Test
        @DisplayName("Should save and retrieve loan by ID")
        void shouldSaveAndRetrieveLoanById() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);

            // When
            LoanEntity saved = repository.save(loan);
            LoanEntity retrieved = repository.findById(saved.getId()).orElse(null);

            // Then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getAccountId()).isEqualTo(loan.getAccountId());
            assertThat(retrieved.getPrincipal()).isEqualByComparingTo(loan.getPrincipal());
        }

        @Test
        @DisplayName("Should save loan with PENDING status")
        void shouldSaveLoanWithPendingStatus() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setStatus(ApplicationStatus.PENDING);

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        }

        @Test
        @DisplayName("Should save loan with APPROVED status")
        void shouldSaveLoanWithApprovedStatus() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setStatus(ApplicationStatus.APPROVED);

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        }

        @Test
        @DisplayName("Should save loan with ACTIVE status")
        void shouldSaveLoanWithActiveStatus() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setStatus(ApplicationStatus.ACTIVE);

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should save loan with PAID_OFF status")
        void shouldSaveLoanWithPaidOffStatus() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setStatus(ApplicationStatus.PAID_OFF);

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.PAID_OFF);
        }

        @Test
        @DisplayName("Should save loan with disbursed at timestamp")
        void shouldSaveLoanWithDisbursedAtTimestamp() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            Instant disbursedAt = Instant.now();
            loan.setDisbursedAt(disbursedAt);

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getDisbursedAt()).isEqualTo(disbursedAt);
        }

        @Test
        @DisplayName("Should save loan with null disbursed at timestamp")
        void shouldSaveLoanWithNullDisbursedAtTimestamp() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setDisbursedAt(null);

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getDisbursedAt()).isNull();
        }

        @Test
        @DisplayName("Should save loan with risk profile JSON")
        void shouldSaveLoanWithRiskProfileJson() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setRiskProfileJson("{\"score\":750,\"risk\":\"LOW\"}");

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getRiskProfileJson()).isEqualTo("{\"score\":750,\"risk\":\"LOW\"}");
        }

        @Test
        @DisplayName("Should save loan with amortization schedule JSON")
        void shouldSaveLoanWithAmortizationScheduleJson() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setAmortizationScheduleJson("[{\"payment\":100,\"principal\":80,\"interest\":20}]");

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getAmortizationScheduleJson()).isEqualTo("[{\"payment\":100,\"principal\":80,\"interest\":20}]");
        }
    }

    @Nested
    @DisplayName("Find By Account ID")
    class FindByAccountIdTests {

        @Test
        @DisplayName("Should find all loans for account")
        void shouldFindAllLoansForAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            repository.save(createTestLoanWithAccount(UUID.randomUUID(), accountId));
            repository.save(createTestLoanWithAccount(UUID.randomUUID(), accountId));
            repository.save(createTestLoanWithAccount(UUID.randomUUID(), accountId));

            // When
            List<LoanEntity> results = repository.findByAccountId(accountId);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results).allMatch(l -> l.getAccountId().equals(accountId));
        }

        @Test
        @DisplayName("Should return empty list when no loans for account")
        void shouldReturnEmptyListWhenNoLoansForAccount() {
            // Given
            UUID nonExistentAccountId = UUID.randomUUID();

            // When
            List<LoanEntity> results = repository.findByAccountId(nonExistentAccountId);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should find loans with different statuses for same account")
        void shouldFindLoansWithDifferentStatusesForSameAccount() {
            // Given
            UUID accountId = UUID.randomUUID();

            LoanEntity loan1 = createTestLoanWithAccount(UUID.randomUUID(), accountId);
            loan1.setStatus(ApplicationStatus.ACTIVE);

            LoanEntity loan2 = createTestLoanWithAccount(UUID.randomUUID(), accountId);
            loan2.setStatus(ApplicationStatus.PAID_OFF);

            LoanEntity loan3 = createTestLoanWithAccount(UUID.randomUUID(), accountId);
            loan3.setStatus(ApplicationStatus.DEFAULT);

            repository.saveAll(List.of(loan1, loan2, loan3));

            // When
            List<LoanEntity> results = repository.findByAccountId(accountId);

            // Then
            assertThat(results).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null risk profile JSON")
        void shouldHandleNullRiskProfileJson() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setRiskProfileJson(null);

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getRiskProfileJson()).isNull();
        }

        @Test
        @DisplayName("Should handle empty risk profile JSON")
        void shouldHandleEmptyRiskProfileJson() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setRiskProfileJson("{}");

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getRiskProfileJson()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle null amortization schedule JSON")
        void shouldHandleNullAmortizationScheduleJson() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setAmortizationScheduleJson(null);

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getAmortizationScheduleJson()).isNull();
        }

        @Test
        @DisplayName("Should handle principal with 4 decimal places")
        void shouldHandlePrincipalWith4DecimalPlaces() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setPrincipal(new BigDecimal("10000.1234"));

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getPrincipal()).isEqualByComparingTo(new BigDecimal("10000.1234"));
        }

        @Test
        @DisplayName("Should handle very large principal amount")
        void shouldHandleVeryLargePrincipalAmount() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setPrincipal(new BigDecimal("10000000.00"));

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getPrincipal()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle interest rate with 4 decimal places")
        void shouldHandleInterestRateWith4DecimalPlaces() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setInterestRate(new BigDecimal("0.1234"));

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getInterestRate()).isEqualByComparingTo(new BigDecimal("0.1234"));
        }

        @Test
        @DisplayName("Should handle zero outstanding balance")
        void shouldHandleZeroOutstandingBalance() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan.setOutstandingBalance(BigDecimal.ZERO);

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getOutstandingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle deleting loans")
        void shouldHandleDeletingLoans() {
            // Given
            UUID id = UUID.randomUUID();
            LoanEntity loan = createTestLoan(id);
            loan = repository.save(loan);

            // When
            repository.delete(loan);

            // Then
            assertThat(repository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should handle deleting all loans")
        void shouldHandleDeletingAllLoans() {
            // Given
            List<LoanEntity> loans = List.of(
                    createTestLoan(UUID.randomUUID()),
                    createTestLoan(UUID.randomUUID()),
                    createTestLoan(UUID.randomUUID())
            );
            repository.saveAll(loans);

            // When
            repository.deleteAll();

            // Then
            assertThat(repository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should handle loan with all fields set")
        void shouldHandleLoanWithAllFieldsSet() {
            // Given
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            LoanEntity loan = new LoanEntity();
            loan.setId(id);
            loan.setAccountId(accountId);
            loan.setPrincipal(new BigDecimal("50000.00"));
            loan.setOutstandingBalance(new BigDecimal("45000.00"));
            loan.setInterestRate(new BigDecimal("0.1050"));
            loan.setTermMonths(36);
            loan.setMonthlyPayment(new BigDecimal("1500.00"));
            loan.setStatus(ApplicationStatus.ACTIVE);
            loan.setCreatedAt(Instant.now());
            loan.setDisbursedAt(Instant.now());
            loan.setRiskProfileJson("{\"score\":750,\"risk\":\"LOW\"}");
            loan.setAmortizationScheduleJson("[{\"payment\":1500,\"principal\":1000,\"interest\":500}]");

            // When
            LoanEntity saved = repository.save(loan);

            // Then
            assertThat(saved.getId()).isEqualTo(id);
            assertThat(saved.getAccountId()).isEqualTo(accountId);
            assertThat(saved.getPrincipal()).isEqualByComparingTo(new BigDecimal("50000.00"));
            assertThat(saved.getOutstandingBalance()).isEqualByComparingTo(new BigDecimal("45000.00"));
            assertThat(saved.getInterestRate()).isEqualByComparingTo(new BigDecimal("0.1050"));
            assertThat(saved.getTermMonths()).isEqualTo(36);
            assertThat(saved.getMonthlyPayment()).isEqualByComparingTo(new BigDecimal("1500.00"));
            assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.ACTIVE);
            assertThat(saved.getRiskProfileJson()).isEqualTo("{\"score\":750,\"risk\":\"LOW\"}");
            assertThat(saved.getAmortizationScheduleJson()).isEqualTo("[{\"payment\":1500,\"principal\":1000,\"interest\":500}]");
        }
    }

    /**
     * Helper method to create a test loan.
     */
    private LoanEntity createTestLoan(UUID id) {
        LoanEntity loan = new LoanEntity();
        loan.setId(id);
        loan.setAccountId(UUID.randomUUID());
        loan.setPrincipal(new BigDecimal("10000.00"));
        loan.setOutstandingBalance(new BigDecimal("10000.00"));
        loan.setInterestRate(new BigDecimal("0.1000"));
        loan.setTermMonths(12);
        loan.setMonthlyPayment(new BigDecimal("879.16"));
        loan.setStatus(ApplicationStatus.PENDING);
        loan.setCreatedAt(Instant.now());
        return loan;
    }

    /**
     * Helper method to create a test loan with specific account.
     */
    private LoanEntity createTestLoanWithAccount(UUID id, UUID accountId) {
        LoanEntity loan = createTestLoan(id);
        loan.setAccountId(accountId);
        return loan;
    }
}
