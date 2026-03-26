package com.neobank.loans.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neobank.loans.AmortizationEntry;
import com.neobank.loans.ApplicationStatus;
import com.neobank.loans.LoanDetails;
import com.neobank.loans.RiskProfile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Mapper between LoanEntity and domain objects.
 */
@Component
class LoanMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert a LoanApplicationRequest to a LoanEntity.
     */
    LoanEntity toEntity(UUID id, UUID accountId, java.math.BigDecimal principal,
                        int termMonths, RiskProfile riskProfile) {
        LoanEntity entity = new LoanEntity();
        entity.setId(id);
        entity.setAccountId(accountId);
        entity.setPrincipal(principal);
        entity.setOutstandingBalance(principal);
        entity.setTermMonths(termMonths);
        entity.setStatus(ApplicationStatus.PENDING);
        entity.setCreatedAt(Instant.now());

        try {
            entity.setRiskProfileJson(objectMapper.writeValueAsString(riskProfile));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize risk profile", e);
        }

        return entity;
    }

    /**
     * Convert a LoanEntity to LoanDetails.
     */
    LoanDetails toDetails(LoanEntity entity, List<AmortizationEntry> schedule) {
        return new LoanDetails(
                entity.getId(),
                entity.getAccountId(),
                entity.getPrincipal(),
                entity.getOutstandingBalance(),
                entity.getInterestRate(),
                entity.getTermMonths(),
                entity.getMonthlyPayment(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getDisbursedAt(),
                schedule
        );
    }

    /**
     * Parse amortization schedule from JSON.
     */
    @SuppressWarnings("unchecked")
    List<AmortizationEntry> parseSchedule(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse amortization schedule", e);
        }
    }

    /**
     * Serialize amortization schedule to JSON.
     */
    String serializeSchedule(List<AmortizationEntry> schedule) {
        try {
            return objectMapper.writeValueAsString(schedule);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize amortization schedule", e);
        }
    }
}
