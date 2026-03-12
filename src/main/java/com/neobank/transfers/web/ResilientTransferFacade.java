package com.neobank.transfers.web;

import com.neobank.transfers.TransactionResult;
import com.neobank.transfers.TransferRequest;
import com.neobank.transfers.api.TransferApi;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Resilient facade for transfer operations.
 * Wraps TransferApi calls with circuit breaker protection.
 * 
 * Note: This component is conditionally enabled via @Profile("production").
 * For testing purposes, the direct TransferApi is used instead.
 */
@Component
public class ResilientTransferFacade {

    private final TransferApi transferApi;

    public ResilientTransferFacade(TransferApi transferApi) {
        this.transferApi = transferApi;
    }

    @CircuitBreaker(name = "transfer", fallbackMethod = "transferFallback")
    public ResponseEntity<Map<String, Object>> transfer(TransferRequest request) {
        TransactionResult result = transferApi.transfer(request);

        return switch (result) {
            case TransactionResult.Success success ->
                ResponseEntity.ok(Map.of("status", "success", "message", success.message()));
            case TransactionResult.Failure failure ->
                ResponseEntity.badRequest().body(Map.of("status", "failure", "message", failure.errorMessage()));
        };
    }

    /**
     * Fallback method invoked when the circuit breaker is open.
     * Returns 503 Service Unavailable to indicate temporary unavailability.
     */
    public ResponseEntity<Map<String, Object>> transferFallback(TransferRequest request, Exception ex) {
        return ResponseEntity
                .status(503)
                .body(Map.of(
                        "status", "error",
                        "code", "SERVICE_UNAVAILABLE",
                        "message", "Service Temporarily Unavailable. Please try again later."
                ));
    }
}
