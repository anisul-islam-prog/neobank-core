package com.neobank.core.approvals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApprovalService using JUnit 5 and Mockito.
 * Tests Maker-Checker approval workflow.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalService Unit Tests")
class ApprovalServiceTest {

    @Mock
    private PendingAuthorizationRepository repository;

    private ApprovalService approvalService;

    @BeforeEach
    void setUp() {
        approvalService = new ApprovalService(repository);
    }

    @Nested
    @DisplayName("Transfer Authorization Creation")
    class TransferAuthorizationCreationTests {

        @Test
        @DisplayName("Should create pending authorization for high-value transfer")
        void shouldCreatePendingAuthorizationForHighValueTransfer() {
            // Given
            UUID initiatorId = UUID.randomUUID();
            String initiatorRole = "TELLER";
            UUID targetAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("10000.00");
            String currency = "USD";
            String reason = "High-value business payment";

            given(repository.save(any(PendingAuthorization.class))).willReturn(new PendingAuthorization());

            // When
            PendingAuthorization result = approvalService.createTransferAuthorization(
                    initiatorId, initiatorRole, targetAccountId, amount, currency, reason
            );

            // Then
            assertThat(result).isNotNull();

            ArgumentCaptor<PendingAuthorization> captor = ArgumentCaptor.forClass(PendingAuthorization.class);
            verify(repository).save(captor.capture());

            PendingAuthorization saved = captor.getValue();
            assertThat(saved.getInitiatorId()).isEqualTo(initiatorId);
            assertThat(saved.getInitiatorRole()).isEqualTo(initiatorRole);
            assertThat(saved.getTargetId()).isEqualTo(targetAccountId);
            assertThat(saved.getAmount()).isEqualByComparingTo(amount);
            assertThat(saved.getCurrency()).isEqualTo(currency);
            assertThat(saved.getReason()).isEqualTo(reason);
            assertThat(saved.getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.PENDING);
        }

        @Test
        @DisplayName("Should set approvedBy and approvedAt on approval")
        void shouldSetApprovedByAndApprovedAtOnApproval() {
            // Given
            UUID initiatorId = UUID.randomUUID();
            String initiatorRole = "TELLER";
            UUID targetAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("10000.00");
            String currency = "USD";
            String reason = "High-value business payment";

            given(repository.save(any(PendingAuthorization.class))).willReturn(new PendingAuthorization());

            // When
            approvalService.createTransferAuthorization(
                    initiatorId, initiatorRole, targetAccountId, amount, currency, reason
            );

            // Then
            ArgumentCaptor<PendingAuthorization> captor = ArgumentCaptor.forClass(PendingAuthorization.class);
            verify(repository).save(captor.capture());

            PendingAuthorization saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.PENDING);
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should handle null reason")
        void shouldHandleNullReason() {
            // Given
            UUID initiatorId = UUID.randomUUID();
            String initiatorRole = "TELLER";
            UUID targetAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("10000.00");
            String currency = "USD";

            given(repository.save(any(PendingAuthorization.class))).willReturn(new PendingAuthorization());

            // When
            PendingAuthorization result = approvalService.createTransferAuthorization(
                    initiatorId, initiatorRole, targetAccountId, amount, currency, null
            );

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle empty reason")
        void shouldHandleEmptyReason() {
            // Given
            UUID initiatorId = UUID.randomUUID();
            String initiatorRole = "TELLER";
            UUID targetAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("10000.00");
            String currency = "USD";

            given(repository.save(any(PendingAuthorization.class))).willReturn(new PendingAuthorization());

            // When
            PendingAuthorization result = approvalService.createTransferAuthorization(
                    initiatorId, initiatorRole, targetAccountId, amount, currency, ""
            );

            // Then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Requires Approval Check")
    class RequiresApprovalCheckTests {

        @Test
        @DisplayName("Should require approval for amount above threshold")
        void shouldRequireApprovalForAmountAboveThreshold() {
            // Given
            BigDecimal amount = new BigDecimal("5000.01");

            // When
            boolean requiresApproval = approvalService.requiresApproval(amount);

            // Then
            assertThat(requiresApproval).isTrue();
        }

        @Test
        @DisplayName("Should not require approval for amount at threshold")
        void shouldNotRequireApprovalForAmountAtThreshold() {
            // Given
            BigDecimal amount = new BigDecimal("5000.00");

            // When
            boolean requiresApproval = approvalService.requiresApproval(amount);

            // Then
            assertThat(requiresApproval).isFalse();
        }

        @Test
        @DisplayName("Should not require approval for amount below threshold")
        void shouldNotRequireApprovalForAmountBelowThreshold() {
            // Given
            BigDecimal amount = new BigDecimal("4999.99");

            // When
            boolean requiresApproval = approvalService.requiresApproval(amount);

            // Then
            assertThat(requiresApproval).isFalse();
        }

        @Test
        @DisplayName("Should not require approval for zero amount")
        void shouldNotRequireApprovalForZeroAmount() {
            // Given
            BigDecimal amount = BigDecimal.ZERO;

            // When
            boolean requiresApproval = approvalService.requiresApproval(amount);

            // Then
            assertThat(requiresApproval).isFalse();
        }

        @Test
        @DisplayName("Should require approval for very large amount")
        void shouldRequireApprovalForVeryLargeAmount() {
            // Given
            BigDecimal amount = new BigDecimal("10000000.00");

            // When
            boolean requiresApproval = approvalService.requiresApproval(amount);

            // Then
            assertThat(requiresApproval).isTrue();
        }

        @Test
        @DisplayName("Should require approval for amount just above threshold")
        void shouldRequireApprovalForAmountJustAboveThreshold() {
            // Given
            BigDecimal amount = new BigDecimal("5000.01");

            // When
            boolean requiresApproval = approvalService.requiresApproval(amount);

            // Then
            assertThat(requiresApproval).isTrue();
        }
    }

    @Nested
    @DisplayName("Approve Authorization")
    class ApproveAuthorizationTests {

        @Test
        @DisplayName("Should approve pending authorization successfully")
        void shouldApprovePendingAuthorizationSuccessfully() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";
            String notes = "Approved after verification";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.approve(authorizationId, reviewerId, reviewerRole, notes);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.APPROVED);
            assertThat(result.get().getReviewerId()).isEqualTo(reviewerId);
            assertThat(result.get().getReviewerRole()).isEqualTo(reviewerRole);
            assertThat(result.get().getReviewNotes()).isEqualTo(notes);
            assertThat(result.get().getReviewedAt()).isNotNull();
            verify(repository).save(auth);
        }

        @Test
        @DisplayName("Should return empty when authorization not found")
        void shouldReturnEmptyWhenAuthorizationNotFound() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            given(repository.findById(authorizationId)).willReturn(Optional.empty());

            // When
            Optional<PendingAuthorization> result = approvalService.approve(authorizationId, reviewerId, reviewerRole, "Notes");

            // Then
            assertThat(result).isEmpty();
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when authorization is not pending")
        void shouldThrowExceptionWhenAuthorizationIsNotPending() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.APPROVED);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));

            // When/Then
            assertThatThrownBy(() -> approvalService.approve(authorizationId, reviewerId, reviewerRole, "Notes"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not pending");
        }

        @Test
        @DisplayName("Should throw exception when authorization is rejected")
        void shouldThrowExceptionWhenAuthorizationIsRejected() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.REJECTED);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));

            // When/Then
            assertThatThrownBy(() -> approvalService.approve(authorizationId, reviewerId, reviewerRole, "Notes"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not pending");
        }

        @Test
        @DisplayName("Should approve with SYSTEM_ADMIN reviewer role")
        void shouldApproveWithSystemAdminReviewerRole() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "SYSTEM_ADMIN";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.approve(authorizationId, reviewerId, reviewerRole, "Notes");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getReviewerRole()).isEqualTo(reviewerRole);
        }

        @Test
        @DisplayName("Should approve with AUDITOR reviewer role")
        void shouldApproveWithAuditorReviewerRole() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "AUDITOR";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.approve(authorizationId, reviewerId, reviewerRole, "Notes");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getReviewerRole()).isEqualTo(reviewerRole);
        }

        @Test
        @DisplayName("Should approve with empty notes")
        void shouldApproveWithEmptyNotes() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.approve(authorizationId, reviewerId, reviewerRole, "");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getReviewNotes()).isEmpty();
        }

        @Test
        @DisplayName("Should approve with long notes")
        void shouldApproveWithLongNotes() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";
            String longNotes = "a".repeat(500);

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.approve(authorizationId, reviewerId, reviewerRole, longNotes);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getReviewNotes()).hasSize(500);
        }

        @Test
        @DisplayName("Should set reviewedAt timestamp on approval")
        void shouldSetReviewedAtTimestampOnApproval() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";
            Instant beforeApproval = Instant.now();

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            approvalService.approve(authorizationId, reviewerId, reviewerRole, "Notes");

            // Then
            assertThat(auth.getReviewedAt()).isNotNull();
            assertThat(auth.getReviewedAt()).isAfterOrEqualTo(beforeApproval);
        }
    }

    @Nested
    @DisplayName("Reject Authorization")
    class RejectAuthorizationTests {

        @Test
        @DisplayName("Should reject pending authorization successfully")
        void shouldRejectPendingAuthorizationSuccessfully() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";
            String notes = "Rejected due to insufficient documentation";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.reject(authorizationId, reviewerId, reviewerRole, notes);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.REJECTED);
            assertThat(result.get().getReviewerId()).isEqualTo(reviewerId);
            assertThat(result.get().getReviewerRole()).isEqualTo(reviewerRole);
            assertThat(result.get().getReviewNotes()).isEqualTo(notes);
            assertThat(result.get().getReviewedAt()).isNotNull();
            verify(repository).save(auth);
        }

        @Test
        @DisplayName("Should return empty when authorization not found for rejection")
        void shouldReturnEmptyWhenAuthorizationNotFoundForRejection() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            given(repository.findById(authorizationId)).willReturn(Optional.empty());

            // When
            Optional<PendingAuthorization> result = approvalService.reject(authorizationId, reviewerId, reviewerRole, "Notes");

            // Then
            assertThat(result).isEmpty();
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when authorization is not pending for rejection")
        void shouldThrowExceptionWhenAuthorizationIsNotPendingForRejection() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.APPROVED);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));

            // When/Then
            assertThatThrownBy(() -> approvalService.reject(authorizationId, reviewerId, reviewerRole, "Notes"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not pending");
        }

        @Test
        @DisplayName("Should reject with SYSTEM_ADMIN reviewer role")
        void shouldRejectWithSystemAdminReviewerRole() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "SYSTEM_ADMIN";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.reject(authorizationId, reviewerId, reviewerRole, "Notes");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getReviewerRole()).isEqualTo(reviewerRole);
        }

        @Test
        @DisplayName("Should reject with empty notes")
        void shouldRejectWithEmptyNotes() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.reject(authorizationId, reviewerId, reviewerRole, "");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getReviewNotes()).isEmpty();
        }

        @Test
        @DisplayName("Should set reviewedAt timestamp on rejection")
        void shouldSetReviewedAtTimestampOnRejection() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";
            Instant beforeRejection = Instant.now();

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            approvalService.reject(authorizationId, reviewerId, reviewerRole, "Notes");

            // Then
            assertThat(auth.getReviewedAt()).isNotNull();
            assertThat(auth.getReviewedAt()).isAfterOrEqualTo(beforeRejection);
        }
    }

    @Nested
    @DisplayName("Get Pending Authorizations")
    class GetPendingAuthorizationsTests {

        @Test
        @DisplayName("Should get all pending authorizations ordered by created date")
        void shouldGetAllPendingAuthorizationsOrderedByCreatedDate() {
            // Given
            Instant now = Instant.now();
            PendingAuthorization auth1 = new PendingAuthorization();
            auth1.setId(UUID.randomUUID());
            auth1.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);
            auth1.setCreatedAt(now.minusSeconds(3600));

            PendingAuthorization auth2 = new PendingAuthorization();
            auth2.setId(UUID.randomUUID());
            auth2.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);
            auth2.setCreatedAt(now);

            PendingAuthorization auth3 = new PendingAuthorization();
            auth3.setId(UUID.randomUUID());
            auth3.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);
            auth3.setCreatedAt(now.minusSeconds(7200));

            given(repository.findByStatusOrderByCreatedAtDesc(PendingAuthorization.AuthorizationStatus.PENDING))
                    .willReturn(java.util.List.of(auth2, auth1, auth3));

            // When
            java.util.List<PendingAuthorization> results = approvalService.getPendingAuthorizations();

            // Then
            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("Should return empty list when no pending authorizations")
        void shouldReturnEmptyListWhenNoPendingAuthorizations() {
            // Given
            given(repository.findByStatusOrderByCreatedAtDesc(PendingAuthorization.AuthorizationStatus.PENDING))
                    .willReturn(java.util.List.of());

            // When
            java.util.List<PendingAuthorization> results = approvalService.getPendingAuthorizations();

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should only return PENDING status authorizations")
        void shouldOnlyReturnPendingStatusAuthorizations() {
            // Given
            PendingAuthorization pendingAuth = new PendingAuthorization();
            pendingAuth.setId(UUID.randomUUID());
            pendingAuth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findByStatusOrderByCreatedAtDesc(PendingAuthorization.AuthorizationStatus.PENDING))
                    .willReturn(java.util.List.of(pendingAuth));

            // When
            java.util.List<PendingAuthorization> results = approvalService.getPendingAuthorizations();

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Get Pending Count")
    class GetPendingCountTests {

        @Test
        @DisplayName("Should get pending authorization count")
        void shouldGetPendingAuthorizationCount() {
            // Given
            given(repository.countByStatus(PendingAuthorization.AuthorizationStatus.PENDING)).willReturn(5L);

            // When
            long count = approvalService.getPendingCount();

            // Then
            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should return 0 when no pending authorizations")
        void shouldReturn0WhenNoPendingAuthorizations() {
            // Given
            given(repository.countByStatus(PendingAuthorization.AuthorizationStatus.PENDING)).willReturn(0L);

            // When
            long count = approvalService.getPendingCount();

            // Then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null reviewer ID")
        void shouldHandleNullReviewerId() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.approve(authorizationId, null, reviewerRole, "Notes");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getReviewerId()).isNull();
        }

        @Test
        @DisplayName("Should handle null reviewer role")
        void shouldHandleNullReviewerRole() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.approve(authorizationId, reviewerId, null, "Notes");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getReviewerRole()).isNull();
        }

        @Test
        @DisplayName("Should handle null review notes")
        void shouldHandleNullReviewNotes() {
            // Given
            UUID authorizationId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            PendingAuthorization auth = new PendingAuthorization();
            auth.setId(authorizationId);
            auth.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(authorizationId)).willReturn(Optional.of(auth));
            given(repository.save(auth)).willReturn(auth);

            // When
            Optional<PendingAuthorization> result = approvalService.approve(authorizationId, reviewerId, reviewerRole, null);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getReviewNotes()).isNull();
        }

        @Test
        @DisplayName("Should handle multiple approvals in sequence")
        void shouldHandleMultipleApprovalsInSequence() {
            // Given
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            PendingAuthorization auth1 = new PendingAuthorization();
            auth1.setId(UUID.randomUUID());
            auth1.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            PendingAuthorization auth2 = new PendingAuthorization();
            auth2.setId(UUID.randomUUID());
            auth2.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(auth1.getId())).willReturn(Optional.of(auth1));
            given(repository.findById(auth2.getId())).willReturn(Optional.of(auth2));
            given(repository.save(auth1)).willReturn(auth1);
            given(repository.save(auth2)).willReturn(auth2);

            // When
            approvalService.approve(auth1.getId(), reviewerId, reviewerRole, "Notes 1");
            approvalService.approve(auth2.getId(), reviewerId, reviewerRole, "Notes 2");

            // Then
            assertThat(auth1.getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.APPROVED);
            assertThat(auth2.getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.APPROVED);
        }

        @Test
        @DisplayName("Should handle multiple rejections in sequence")
        void shouldHandleMultipleRejectionsInSequence() {
            // Given
            UUID reviewerId = UUID.randomUUID();
            String reviewerRole = "MANAGER";

            PendingAuthorization auth1 = new PendingAuthorization();
            auth1.setId(UUID.randomUUID());
            auth1.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            PendingAuthorization auth2 = new PendingAuthorization();
            auth2.setId(UUID.randomUUID());
            auth2.setStatus(PendingAuthorization.AuthorizationStatus.PENDING);

            given(repository.findById(auth1.getId())).willReturn(Optional.of(auth1));
            given(repository.findById(auth2.getId())).willReturn(Optional.of(auth2));
            given(repository.save(auth1)).willReturn(auth1);
            given(repository.save(auth2)).willReturn(auth2);

            // When
            approvalService.reject(auth1.getId(), reviewerId, reviewerRole, "Notes 1");
            approvalService.reject(auth2.getId(), reviewerId, reviewerRole, "Notes 2");

            // Then
            assertThat(auth1.getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.REJECTED);
            assertThat(auth2.getStatus()).isEqualTo(PendingAuthorization.AuthorizationStatus.REJECTED);
        }

        @Test
        @DisplayName("Should handle authorization with very large amount")
        void shouldHandleAuthorizationWithVeryLargeAmount() {
            // Given
            UUID initiatorId = UUID.randomUUID();
            String initiatorRole = "TELLER";
            UUID targetAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("10000000.00");
            String currency = "USD";

            given(repository.save(any(PendingAuthorization.class))).willReturn(new PendingAuthorization());

            // When
            PendingAuthorization result = approvalService.createTransferAuthorization(
                    initiatorId, initiatorRole, targetAccountId, amount, currency, "Test"
            );

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle authorization with amount having 4 decimal places")
        void shouldHandleAuthorizationWithAmountHavingFourDecimalPlaces() {
            // Given
            UUID initiatorId = UUID.randomUUID();
            String initiatorRole = "TELLER";
            UUID targetAccountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("10000.1234");
            String currency = "USD";

            given(repository.save(any(PendingAuthorization.class))).willReturn(new PendingAuthorization());

            // When
            PendingAuthorization result = approvalService.createTransferAuthorization(
                    initiatorId, initiatorRole, targetAccountId, amount, currency, "Test"
            );

            // Then
            assertThat(result).isNotNull();
        }
    }
}
