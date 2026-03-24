package com.neobank.core.approvals.api;

import com.neobank.core.approvals.PendingAuthorization;
import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API for approval operations.
 */
@NamedInterface("approval-api")
public interface ApprovalApi {

    List<PendingAuthorization> getPendingAuthorizations();

    Optional<PendingAuthorization> approve(UUID authorizationId, UUID reviewerId, String reviewerRole, String notes);

    Optional<PendingAuthorization> reject(UUID authorizationId, UUID reviewerId, String reviewerRole, String notes);

    long getPendingCount();
}
