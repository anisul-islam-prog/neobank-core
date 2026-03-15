package com.neobank.auth.internal.docs;

/**
 * Request DTO for generating a documentation access token.
 *
 * @param description optional description for the token
 * @param durationHours validity duration in hours (default 24)
 */
public record DocTokenRequest(
        String description,
        Long durationHours
) {
    public DocTokenRequest {
        if (durationHours != null && (durationHours <= 0 || durationHours > 720)) {
            throw new IllegalArgumentException("durationHours must be between 1 and 720");
        }
    }

    public long getDurationHours() {
        return durationHours != null ? durationHours : 24;
    }
}
