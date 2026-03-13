package com.neobank.loans.internal;

import com.neobank.accounts.api.AccountApi;
import com.neobank.loans.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Internal loan service implementing the loan origination workflow.
 * Workflow: Applied -> Calculated -> Approved -> Disbursed
 */
@Service
@Transactional
class LoanService implements LoanApi {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final InterestEngine interestEngine;
    private final AmortizationSchedule amortizationSchedule;
    private final AccountApi accountApi;

    public LoanService(LoanRepository loanRepository, LoanMapper loanMapper,
                       InterestEngine interestEngine, AmortizationSchedule amortizationSchedule,
                       AccountApi accountApi) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
        this.interestEngine = interestEngine;
        this.amortizationSchedule = amortizationSchedule;
        this.accountApi = accountApi;
    }

    @Override
    public LoanApplicationResult apply(LoanApplicationRequest request) {
        // Create a risk profile (in real app, this would come from credit bureau)
        RiskProfile riskProfile = createRiskProfile(request.accountId());

        // Calculate interest rate using the risk profile directly
        BigDecimal interestRate = interestEngine.calculateInterestRateFor(riskProfile);

        // Calculate monthly payment
        BigDecimal monthlyPayment = interestEngine.calculateMonthlyPayment(
                request.principal(), interestRate, request.termMonths()
        );

        // Create loan entity
        UUID loanId = UUID.randomUUID();
        LoanEntity entity = loanMapper.toEntity(
                loanId,
                request.accountId(),
                request.principal(),
                request.termMonths(),
                riskProfile
        );
        entity.setInterestRate(interestRate);
        entity.setMonthlyPayment(monthlyPayment);

        // Generate amortization schedule
        List<AmortizationEntry> schedule = amortizationSchedule.generateSchedule(
                request.principal(), interestRate, request.termMonths()
        );
        entity.setAmortizationScheduleJson(loanMapper.serializeSchedule(schedule));

        // Save the loan
        loanRepository.save(entity);

        log.info("Loan application created: {} with monthly payment: {}", loanId, monthlyPayment);

        return LoanApplicationResult.approved(loanId, monthlyPayment);
    }

    @Override
    public LoanDetails getLoan(UUID loanId) {
        return loanRepository.findById(loanId)
                .map(entity -> {
                    List<AmortizationEntry> schedule = loanMapper.parseSchedule(entity.getAmortizationScheduleJson());
                    return loanMapper.toDetails(entity, schedule);
                })
                .orElse(null);
    }

    @Override
    public DisbursementResult disburse(UUID loanId) {
        return loanRepository.findById(loanId)
                .map(entity -> {
                    if (entity.getStatus() != ApplicationStatus.APPROVED &&
                        entity.getStatus() != ApplicationStatus.PENDING) {
                        return DisbursementResult.failure(loanId,
                                "Loan must be in PENDING or APPROVED status. Current: " + entity.getStatus());
                    }

                    try {
                        // Disburse funds to borrower's account
                        accountApi.creditAccount(entity.getAccountId(), entity.getPrincipal());

                        // Update loan status
                        entity.setStatus(ApplicationStatus.DISBURSED);
                        entity.setDisbursedAt(Instant.now());
                        loanRepository.save(entity);

                        log.info("Loan disbursed: {} to account: {}", loanId, entity.getAccountId());

                        return DisbursementResult.success(loanId, entity.getDisbursedAt());

                    } catch (Exception e) {
                        log.error("Failed to disburse loan: {}", loanId, e);
                        return DisbursementResult.failure(loanId, "Disbursement failed: " + e.getMessage());
                    }
                })
                .orElseGet(() -> DisbursementResult.failure(loanId, "Loan not found"));
    }

    /**
     * Create a risk profile for the applicant.
     * In a real application, this would fetch data from credit bureaus.
     */
    private RiskProfile createRiskProfile(UUID accountId) {
        // Simulated risk profile - in production, fetch from credit bureau
        return new RiskProfile(
                720,  // Good credit score
                new BigDecimal("0.35"),  // 35% DTI
                5,  // 5 years employment
                new BigDecimal("75000")  // $75k annual income
        );
    }

    /**
     * Approve a loan application.
     * Package-private for internal use.
     */
    void approveLoan(UUID loanId) {
        loanRepository.findById(loanId).ifPresent(entity -> {
            if (entity.getStatus() == ApplicationStatus.PENDING) {
                entity.setStatus(ApplicationStatus.APPROVED);
                loanRepository.save(entity);
                log.info("Loan approved: {}", loanId);
            }
        });
    }
}
