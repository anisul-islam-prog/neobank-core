package com.neobank.auth.internal.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing documentation access tokens.
 * Only accessible by SYSTEM_ADMIN.
 */
@RestController
@RequestMapping("/api/auth/admin/docs")
@Tag(name = "Documentation Access Control", description = "SYSTEM_ADMIN only: Manage Swagger UI access tokens")
@SecurityRequirement(name = "bearerAuth")
public class SysAdminDocController {

    private final DocTokenService tokenService;

    @Value("${neobank.base-url:http://localhost:8080}")
    private String baseUrl;

    public SysAdminDocController(DocTokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Generate a new documentation access token.
     */
    @PostMapping("/tokens")
    @Operation(
            summary = "Generate documentation access token",
            description = """
                    Create a new access token for Swagger UI documentation.
                    
                    **Access Control:**
                    - SYSTEM_ADMIN only
                    
                    **Token Properties:**
                    - Valid for 24 hours by default (configurable up to 720 hours)
                    - Single-use tracking enabled
                    - Can be revoked at any time
                    
                    **Security:**
                    - Token value is only returned once at creation
                    - Subsequent API calls only show token preview
                    - Store the token securely - it cannot be retrieved later
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token generated successfully",
                    content = @Content(schema = @Schema(implementation = DocTokenGenerationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not authorized - SYSTEM_ADMIN role required")
    })
    public ResponseEntity<DocTokenGenerationResponse> generateToken(
            @RequestBody(required = false) DocTokenRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = extractUserId(userDetails);
        String username = userDetails != null ? userDetails.getUsername() : "system";
        
        String description = request != null ? request.description() : "API Documentation Access";
        long durationHours = request != null ? request.getDurationHours() : 24;
        
        DocTokenEntity entity = tokenService.generateToken(userId, username, description, durationHours);
        DocTokenGenerationResponse response = DocTokenGenerationResponse.of(entity, baseUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * List all active documentation tokens.
     */
    @GetMapping("/tokens")
    @Operation(
            summary = "List active documentation tokens",
            description = """
                    Retrieve all active documentation access tokens.
                    
                    **Access Control:**
                    - SYSTEM_ADMIN only
                    
                    **Note:**
                    - Actual token values are NOT returned (security)
                    - Token preview (first 12 chars) shown for identification
                    - Includes usage statistics and expiration info
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of active tokens",
                    content = @Content(schema = @Schema(implementation = DocTokenListResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not authorized - SYSTEM_ADMIN role required")
    })
    public ResponseEntity<List<DocTokenListResponse>> listTokens(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        List<DocTokenEntity> entities = tokenService.listActiveTokens();
        List<DocTokenListResponse> responses = entities.stream()
                .map(DocTokenListResponse::of)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Revoke a specific documentation token.
     */
    @DeleteMapping("/tokens/{tokenId}")
    @Operation(
            summary = "Revoke documentation token",
            description = """
                    Immediately revoke a documentation access token.
                    
                    **Access Control:**
                    - SYSTEM_ADMIN only
                    
                    **Effect:**
                    - Token becomes invalid immediately
                    - Any active Swagger UI sessions will be terminated
                    - Action cannot be undone
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token revoked successfully"),
            @ApiResponse(responseCode = "404", description = "Token not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized - SYSTEM_ADMIN role required")
    })
    public ResponseEntity<Void> revokeToken(
            @Parameter(description = "Token ID to revoke") @PathVariable UUID tokenId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        boolean revoked = tokenService.revokeToken(tokenId);
        if (revoked) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Revoke all documentation tokens.
     */
    @DeleteMapping("/tokens")
    @Operation(
            summary = "Revoke all documentation tokens",
            description = """
                    Revoke ALL active documentation access tokens.
                    
                    **Access Control:**
                    - SYSTEM_ADMIN only
                    
                    **Warning:**
                    - This will invalidate ALL active Swagger UI sessions
                    - All users will need new tokens to access documentation
                    - Use with caution in production environments
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All tokens revoked",
                    content = @Content(schema = @Schema(example = "{\"revoked\": 5}"))),
            @ApiResponse(responseCode = "403", description = "Not authorized - SYSTEM_ADMIN role required")
    })
    public ResponseEntity<RevokeAllResponse> revokeAllTokens(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        int count = tokenService.revokeAllTokensForUser(extractUserId(userDetails));
        return ResponseEntity.ok(new RevokeAllResponse(count));
    }

    /**
     * Extract user ID from UserDetails.
     */
    private UUID extractUserId(UserDetails userDetails) {
        if (userDetails == null) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(userDetails.getUsername().getBytes());
        }
    }

    /**
     * Response for revoke all operation.
     */
    private record RevokeAllResponse(int revoked) {}
}
