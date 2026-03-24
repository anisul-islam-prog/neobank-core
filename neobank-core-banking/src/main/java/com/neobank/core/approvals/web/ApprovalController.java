package com.neobank.core.approvals.web;

import com.neobank.core.approvals.ApprovalService;
import com.neobank.core.approvals.PendingAuthorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for approval operations.
 */
@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /**
     * Get all pending authorizations.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PendingAuthorization>> getPendingAuthorizations() {
        return ResponseEntity.ok(approvalService.getPendingAuthorizations());
    }

    /**
     * Get pending authorization count.
     */
    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        return ResponseEntity.ok(Map.of("count", approvalService.getPendingCount()));
    }

    /**
     * Approve a pending authorization.
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<PendingAuthorization> approve(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID reviewerId,
            @RequestHeader("X-User-Role") String reviewerRole,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String notes = body != null ? body.get("notes") : null;
        return approvalService.approve(id, reviewerId, reviewerRole, notes)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Reject a pending authorization.
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<PendingAuthorization> reject(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID reviewerId,
            @RequestHeader("X-User-Role") String reviewerRole,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String notes = body != null ? body.get("notes") : null;
        return approvalService.reject(id, reviewerId, reviewerRole, notes)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
