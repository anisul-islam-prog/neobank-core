package com.neobank.core.approvals;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for pending authorizations.
 */
public interface PendingAuthorizationRepository extends JpaRepository<PendingAuthorization, UUID> {

    List<PendingAuthorization> findByStatusOrderByCreatedAtDesc(PendingAuthorization.AuthorizationStatus status);

    List<PendingAuthorization> findByActionTypeAndStatus(PendingAuthorization.ActionType actionType, PendingAuthorization.AuthorizationStatus status);

    long countByStatus(PendingAuthorization.AuthorizationStatus status);
}
