package com.neobank.auth.internal.docs;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for documentation token generation.
 *
 * @param tokenId the token entity ID
 * @param token the actual access token (only returned once)
 * @param description the token description
 * @param createdAt when the token was created
 * @param expiresAt when the token expires
 * @param accessUrl the full URL to access Swagger UI with this token
 */
public record DocTokenGenerationResponse(
        UUID tokenId,
        String token,
        String description,
        Instant createdAt,
        Instant expiresAt,
        String accessUrl
) {
    public static DocTokenGenerationResponse of(DocTokenEntity entity, String baseUrl) {
        return new DocTokenGenerationResponse(
                entity.getId(),
                entity.getToken(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                baseUrl + "/swagger-ui.html?access_token=" + entity.getToken()
        );
    }
}
