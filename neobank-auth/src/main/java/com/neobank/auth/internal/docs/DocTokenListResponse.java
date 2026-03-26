package com.neobank.auth.internal.docs;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for listing documentation tokens.
 * Does not include the actual token value for security.
 *
 * @param tokenId the token entity ID
 * @param tokenPreview first 12 characters of token (for identification)
 * @param description the token description
 * @param createdByUsername who created the token
 * @param createdAt when the token was created
 * @param expiresAt when the token expires
 * @param useCount number of times the token has been used
 * @param lastUsedAt last time the token was used
 */
public record DocTokenListResponse(
        UUID tokenId,
        String tokenPreview,
        String description,
        String createdByUsername,
        Instant createdAt,
        Instant expiresAt,
        int useCount,
        Instant lastUsedAt
) {
    public static DocTokenListResponse of(DocTokenEntity entity) {
        String preview = entity.getToken().length() > 12 
                ? entity.getToken().substring(0, 12) + "..." 
                : entity.getToken();
        
        return new DocTokenListResponse(
                entity.getId(),
                preview,
                entity.getDescription(),
                entity.getCreatedByUsername(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getUseCount(),
                entity.getLastUsedAt()
        );
    }
}
