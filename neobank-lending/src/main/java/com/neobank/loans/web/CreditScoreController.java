package com.neobank.loans.web;

import com.neobank.loans.CreditScoreApi;
import com.neobank.loans.CreditScoreResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for credit score operations.
 * Provides endpoints for users to check their credit score.
 */
@RestController
@RequestMapping("/api/loans")
@Tag(name = "Credit Score", description = "APIs for checking and managing credit scores")
@SecurityRequirement(name = "bearerAuth")
public class CreditScoreController {

    private final CreditScoreApi creditScoreApi;

    public CreditScoreController(CreditScoreApi creditScoreApi) {
        this.creditScoreApi = creditScoreApi;
    }

    /**
     * Get the current user's credit score.
     * 
     * @param userDetails the authenticated user from JWT token
     * @return the credit score result
     */
    @GetMapping("/credit-score")
    @Operation(
            summary = "Get your credit score",
            description = """
                    Retrieve your current credit score and risk assessment.
                    
                    The credit score ranges from 0-100:
                    - **0-24**: Low Risk (Excellent)
                    - **25-49**: Medium Risk (Good)
                    - **50-74**: High Risk (Fair)
                    - **75-100**: Very High Risk (Poor)
                    
                    The score is calculated based on:
                    - Credit history
                    - Debt-to-income ratio
                    - Employment stability
                    - Income level
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Credit score retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CreditScoreResult.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            )
    })
    public ResponseEntity<CreditScoreResult> getCreditScore(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        // Extract user ID from authenticated user
        // In production, this would come from the JWT token claims
        UUID userId = parseUserId(userDetails);
        
        CreditScoreResult result = creditScoreApi.getCreditScore(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Parse user ID from UserDetails.
     * In production, this extracts the UUID from JWT claims.
     */
    private UUID parseUserId(UserDetails userDetails) {
        if (userDetails == null) {
            // Generate a demo UUID for unauthenticated access (for testing)
            return UUID.randomUUID();
        }
        
        // Try to parse username as UUID, or generate from hash
        try {
            return UUID.fromString(userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            // Username is not a UUID, generate deterministic UUID from hash
            return UUID.nameUUIDFromBytes(userDetails.getUsername().getBytes());
        }
    }
}
