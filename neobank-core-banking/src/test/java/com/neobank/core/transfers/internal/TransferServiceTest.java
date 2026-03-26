package com.neobank.core.transfers.internal;

import com.neobank.core.accounts.Account;
import com.neobank.core.accounts.api.AccountApi;
import com.neobank.core.approvals.ApprovalService;
import com.neobank.core.approvals.PendingAuthorization;
import com.neobank.core.transfers.MoneyTransferredEvent;
import com.neobank.core.transfers.TransactionResult;
import com.neobank.core.transfers.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransferService using JUnit 5 and Mockito.
 * Tests fund transfer operations and Maker-Checker protocol.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Unit Tests")
class TransferServiceTest {

    @Mock
    private AccountApi accountApi;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferMapper transferMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ApprovalService approvalService;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountApi, transferRepository, transferMapper, eventPublisher, approvalService);
    }

    @Nested
    @DisplayName("Standard Transfers (Below Threshold)")
    class StandardTransferTests {

        @Test
        @DisplayName("Should transfer funds successfully when sufficient balance")
        void shouldTransferFundsSuccessfully() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD", null, null, null
            );

            Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("1000.00"));
            Account toAccount = new Account(toAccountId, "Jane Doe", new BigDecimal("200.00"));
            TransferEntity transferEntity = new TransferEntity();
            transferEntity.setId(UUID.randomUUID());

            given(approvalService.requiresApproval(transferAmount)).willReturn(false);
            given(accountApi.getAccountWithLock(fromAccountId)).willReturn(fromAccount);
            given(accountApi.getAccountWithLock(toAccountId)).willReturn(toAccount);
            given(transferMapper.toEntity(request)).willReturn(transferEntity);
            doNothing().when(transferMapper).toCompletedEntity(transferEntity);
            given(transferRepository.save(transferEntity)).willReturn(transferEntity);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Success.class);
            assertThat(result.message()).isEqualTo("Transfer completed successfully");

            // Verify accounts were updated
            ArgumentCaptor<Account> fromAccountCaptor = ArgumentCaptor.forClass(Account.class);
            ArgumentCaptor<Account> toAccountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountApi).updateAccount(fromAccountCaptor.capture());
            verify(accountApi).updateAccount(toAccountCaptor.capture());

            Account updatedFrom = fromAccountCaptor.getValue();
            Account updatedTo = toAccountCaptor.getValue();

            assertThat(updatedFrom.balance()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(updatedTo.balance()).isEqualByComparingTo(new BigDecimal("700.00"));
        }

        @Test
        @DisplayName("Should fail transfer when insufficient balance")
        void shouldFailTransferWithInsufficientBalance() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("5000.00");
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD", null, null, null
            );

            Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("1000.00"));
            Account toAccount = new Account(toAccountId, "Jane Doe", new BigDecimal("200.00"));

            given(approvalService.requiresApproval(transferAmount)).willReturn(false);
            given(accountApi.getAccountWithLock(fromAccountId)).willReturn(fromAccount);
            given(accountApi.getAccountWithLock(toAccountId)).willReturn(toAccount);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Failure.class);
            assertThat(result.message()).isEqualTo("Insufficient balance");

            // Verify no accounts were updated
            verify(accountApi, never()).updateAccount(any());
            verify(transferRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail transfer when balance equals zero")
        void shouldFailTransferWhenBalanceEqualsZero() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("100.00");
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD", null, null, null
            );

            Account fromAccount = new Account(fromAccountId, "John Doe", BigDecimal.ZERO);
            Account toAccount = new Account(toAccountId, "Jane Doe", new BigDecimal("200.00"));

            given(approvalService.requiresApproval(transferAmount)).willReturn(false);
            given(accountApi.getAccountWithLock(fromAccountId)).willReturn(fromAccount);
            given(accountApi.getAccountWithLock(toAccountId)).willReturn(toAccount);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Failure.class);
            verify(accountApi, never()).updateAccount(any());
        }

        @Test
        @DisplayName("Should transfer full balance successfully")
        void shouldTransferFullBalanceSuccessfully() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("1000.00");
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD", null, null, null
            );

            Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("1000.00"));
            Account toAccount = new Account(toAccountId, "Jane Doe", new BigDecimal("0.00"));
            TransferEntity transferEntity = new TransferEntity();
            transferEntity.setId(UUID.randomUUID());

            given(approvalService.requiresApproval(transferAmount)).willReturn(false);
            given(accountApi.getAccountWithLock(fromAccountId)).willReturn(fromAccount);
            given(accountApi.getAccountWithLock(toAccountId)).willReturn(toAccount);
            given(transferMapper.toEntity(request)).willReturn(transferEntity);
            doNothing().when(transferMapper).toCompletedEntity(transferEntity);
            given(transferRepository.save(transferEntity)).willReturn(transferEntity);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Success.class);

            ArgumentCaptor<Account> fromAccountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountApi).updateAccount(fromAccountCaptor.capture());

            assertThat(fromAccountCaptor.getValue().balance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle transfer to same account")
        void shouldHandleTransferToSameAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("100.00");
            TransferRequest request = new TransferRequest(
                accountId, accountId, transferAmount, "USD", null, null, null
            );

            Account account = new Account(accountId, "John Doe", new BigDecimal("1000.00"));
            TransferEntity transferEntity = new TransferEntity();
            transferEntity.setId(UUID.randomUUID());

            given(approvalService.requiresApproval(transferAmount)).willReturn(false);
            given(accountApi.getAccountWithLock(accountId)).willReturn(account);
            given(transferMapper.toEntity(request)).willReturn(transferEntity);
            doNothing().when(transferMapper).toCompletedEntity(transferEntity);
            given(transferRepository.save(transferEntity)).willReturn(transferEntity);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Success.class);

            // Balance should remain the same (debit and credit same account)
            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountApi, times(2)).updateAccount(accountCaptor.capture());

            // First update: debit
            assertThat(accountCaptor.getAllValues().get(0).balance()).isEqualByComparingTo(new BigDecimal("900.00"));
            // Second update: credit back
            assertThat(accountCaptor.getAllValues().get(1).balance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }
    }

    @Nested
    @DisplayName("Maker-Checker Protocol (High-Value Transfers)")
    class MakerCheckerProtocolTests {

        @Test
        @DisplayName("Should create pending authorization for high-value transfer")
        void shouldCreatePendingAuthorizationForHighValueTransfer() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("10000.00");
            UUID initiatorId = UUID.randomUUID();
            String initiatorRole = "TELLER";

            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD",
                initiatorId, initiatorRole, "High value transfer"
            );

            PendingAuthorization pendingAuth = new PendingAuthorization();
            pendingAuth.setId(UUID.randomUUID());

            given(approvalService.requiresApproval(transferAmount)).willReturn(true);
            given(approvalService.createTransferAuthorization(
                any(), any(), any(), any(), any(), any()
            )).willReturn(pendingAuth);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Pending.class);
            assertThat(result.message()).contains("Transfer requires approval");

            // Verify authorization was created
            verify(approvalService).createTransferAuthorization(
                eq(initiatorId), eq(initiatorRole), eq(toAccountId),
                eq(transferAmount), eq("USD"), any()
            );

            // Verify no account updates for pending transfer
            verify(accountApi, never()).updateAccount(any());
        }

        @Test
        @DisplayName("Should use default initiatorId when not provided")
        void shouldUseDefaultInitiatorIdWhenNotProvided() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("10000.00");

            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD",
                null, null, null
            );

            PendingAuthorization pendingAuth = new PendingAuthorization();
            pendingAuth.setId(UUID.randomUUID());

            given(approvalService.requiresApproval(transferAmount)).willReturn(true);
            given(approvalService.createTransferAuthorization(
                any(), any(), any(), any(), any(), any()
            )).willReturn(pendingAuth);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Pending.class);

            // Verify default initiatorId was used
            ArgumentCaptor<UUID> initiatorIdCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(approvalService).createTransferAuthorization(
                initiatorIdCaptor.capture(), any(), any(), any(), any(), any()
            );
            assertThat(initiatorIdCaptor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("Should use default initiatorRole when not provided")
        void shouldUseDefaultInitiatorRoleWhenNotProvided() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("10000.00");

            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD",
                UUID.randomUUID(), null, null
            );

            PendingAuthorization pendingAuth = new PendingAuthorization();
            pendingAuth.setId(UUID.randomUUID());

            given(approvalService.requiresApproval(transferAmount)).willReturn(true);
            given(approvalService.createTransferAuthorization(
                any(), any(), any(), any(), any(), any()
            )).willReturn(pendingAuth);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Pending.class);

            // Verify default initiatorRole was used
            ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
            verify(approvalService).createTransferAuthorization(
                any(), roleCaptor.capture(), any(), any(), any(), any()
            );
            assertThat(roleCaptor.getValue()).isEqualTo("TELLER");
        }

        @Test
        @DisplayName("Should use provided reason when provided")
        void shouldUseProvidedReasonWhenProvided() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("10000.00");
            String customReason = "Business payment for invoice #12345";

            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD",
                UUID.randomUUID(), "TELLER", customReason
            );

            PendingAuthorization pendingAuth = new PendingAuthorization();
            pendingAuth.setId(UUID.randomUUID());

            given(approvalService.requiresApproval(transferAmount)).willReturn(true);
            given(approvalService.createTransferAuthorization(
                any(), any(), any(), any(), any(), any()
            )).willReturn(pendingAuth);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Pending.class);

            // Verify custom reason was used
            ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
            verify(approvalService).createTransferAuthorization(
                any(), any(), any(), any(), any(), reasonCaptor.capture()
            );
            assertThat(reasonCaptor.getValue()).contains(customReason);
        }

        @Test
        @DisplayName("Should publish TransferPendingEvent for high-value transfer")
        void shouldPublishTransferPendingEventForHighValueTransfer() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("10000.00");
            UUID authorizationId = UUID.randomUUID();

            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD",
                UUID.randomUUID(), "TELLER", null
            );

            PendingAuthorization pendingAuth = new PendingAuthorization();
            pendingAuth.setId(authorizationId);

            given(approvalService.requiresApproval(transferAmount)).willReturn(true);
            given(approvalService.createTransferAuthorization(
                any(), any(), any(), any(), any(), any()
            )).willReturn(pendingAuth);

            // When
            transferService.transfer(request);

            // Then
            ArgumentCaptor<TransferService.TransferPendingEvent> eventCaptor = ArgumentCaptor.forClass(TransferService.TransferPendingEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            TransferService.TransferPendingEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.authorizationId()).isEqualTo(authorizationId);
            assertThat(publishedEvent.fromId()).isEqualTo(fromAccountId);
            assertThat(publishedEvent.toId()).isEqualTo(toAccountId);
            assertThat(publishedEvent.amount()).isEqualByComparingTo(transferAmount);
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEventsTests {

        @Test
        @DisplayName("Should publish MoneyTransferredEvent on successful transfer")
        void shouldPublishMoneyTransferredEventOnSuccessfulTransfer() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            UUID transferId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD", null, null, null
            );

            Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("1000.00"));
            Account toAccount = new Account(toAccountId, "Jane Doe", new BigDecimal("200.00"));
            TransferEntity transferEntity = new TransferEntity();
            transferEntity.setId(transferId);

            given(approvalService.requiresApproval(transferAmount)).willReturn(false);
            given(accountApi.getAccountWithLock(fromAccountId)).willReturn(fromAccount);
            given(accountApi.getAccountWithLock(toAccountId)).willReturn(toAccount);
            given(transferMapper.toEntity(request)).willReturn(transferEntity);
            doNothing().when(transferMapper).toCompletedEntity(transferEntity);
            given(transferRepository.save(transferEntity)).willReturn(transferEntity);

            // When
            transferService.transfer(request);

            // Then
            ArgumentCaptor<MoneyTransferredEvent> eventCaptor = ArgumentCaptor.forClass(MoneyTransferredEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            MoneyTransferredEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.transferId()).isEqualTo(transferId);
            assertThat(publishedEvent.senderId()).isEqualTo(fromAccountId);
            assertThat(publishedEvent.receiverId()).isEqualTo(toAccountId);
            assertThat(publishedEvent.amount()).isEqualByComparingTo(transferAmount);
        }

        @Test
        @DisplayName("Should not publish event on failed transfer")
        void shouldNotPublishEventOnFailedTransfer() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("5000.00");
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD", null, null, null
            );

            Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("1000.00"));
            Account toAccount = new Account(toAccountId, "Jane Doe", new BigDecimal("200.00"));

            given(approvalService.requiresApproval(transferAmount)).willReturn(false);
            given(accountApi.getAccountWithLock(fromAccountId)).willReturn(fromAccount);
            given(accountApi.getAccountWithLock(toAccountId)).willReturn(toAccount);

            // When
            transferService.transfer(request);

            // Then
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should not publish MoneyTransferredEvent for pending transfer")
        void shouldNotPublishMoneyTransferredEventForPendingTransfer() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("10000.00");
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD", null, null, null
            );

            PendingAuthorization pendingAuth = new PendingAuthorization();
            pendingAuth.setId(UUID.randomUUID());

            given(approvalService.requiresApproval(transferAmount)).willReturn(true);
            given(approvalService.createTransferAuthorization(
                any(), any(), any(), any(), any(), any()
            )).willReturn(pendingAuth);

            // When
            transferService.transfer(request);

            // Then
            verify(eventPublisher, never()).publishEvent(any(MoneyTransferredEvent.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle transfer at exact threshold amount")
        void shouldHandleTransferAtExactThresholdAmount() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("5000.00"); // Exact threshold
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD", null, null, null
            );

            Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("10000.00"));
            Account toAccount = new Account(toAccountId, "Jane Doe", new BigDecimal("200.00"));
            TransferEntity transferEntity = new TransferEntity();
            transferEntity.setId(UUID.randomUUID());

            // Amount at threshold should NOT require approval (only > threshold)
            given(approvalService.requiresApproval(transferAmount)).willReturn(false);
            given(accountApi.getAccountWithLock(fromAccountId)).willReturn(fromAccount);
            given(accountApi.getAccountWithLock(toAccountId)).willReturn(toAccount);
            given(transferMapper.toEntity(request)).willReturn(transferEntity);
            doNothing().when(transferMapper).toCompletedEntity(transferEntity);
            given(transferRepository.save(transferEntity)).willReturn(transferEntity);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Success.class);
        }

        @Test
        @DisplayName("Should handle transfer just above threshold amount")
        void shouldHandleTransferJustAboveThresholdAmount() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("5000.01"); // Just above threshold
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "USD", null, null, null
            );

            PendingAuthorization pendingAuth = new PendingAuthorization();
            pendingAuth.setId(UUID.randomUUID());

            // Amount above threshold should require approval
            given(approvalService.requiresApproval(transferAmount)).willReturn(true);
            given(approvalService.createTransferAuthorization(
                any(), any(), any(), any(), any(), any()
            )).willReturn(pendingAuth);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Pending.class);
        }

        @Test
        @DisplayName("Should handle transfer with custom currency")
        void shouldHandleTransferWithCustomCurrency() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, "EUR", null, null, null
            );

            Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("1000.00"));
            Account toAccount = new Account(toAccountId, "Jane Doe", new BigDecimal("200.00"));
            TransferEntity transferEntity = new TransferEntity();
            transferEntity.setId(UUID.randomUUID());

            given(approvalService.requiresApproval(transferAmount)).willReturn(false);
            given(accountApi.getAccountWithLock(fromAccountId)).willReturn(fromAccount);
            given(accountApi.getAccountWithLock(toAccountId)).willReturn(toAccount);
            given(transferMapper.toEntity(request)).willReturn(transferEntity);
            doNothing().when(transferMapper).toCompletedEntity(transferEntity);
            given(transferRepository.save(transferEntity)).willReturn(transferEntity);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Success.class);
        }

        @Test
        @DisplayName("Should handle transfer with null currency (defaults to USD)")
        void shouldHandleTransferWithNullCurrency() {
            // Given
            UUID fromAccountId = UUID.randomUUID();
            UUID toAccountId = UUID.randomUUID();
            BigDecimal transferAmount = new BigDecimal("500.00");
            TransferRequest request = new TransferRequest(
                fromAccountId, toAccountId, transferAmount, null, null, null, null
            );

            Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("1000.00"));
            Account toAccount = new Account(toAccountId, "Jane Doe", new BigDecimal("200.00"));
            TransferEntity transferEntity = new TransferEntity();
            transferEntity.setId(UUID.randomUUID());

            given(approvalService.requiresApproval(transferAmount)).willReturn(false);
            given(accountApi.getAccountWithLock(fromAccountId)).willReturn(fromAccount);
            given(accountApi.getAccountWithLock(toAccountId)).willReturn(toAccount);
            given(transferMapper.toEntity(request)).willReturn(transferEntity);
            doNothing().when(transferMapper).toCompletedEntity(transferEntity);
            given(transferRepository.save(transferEntity)).willReturn(transferEntity);

            // When
            TransactionResult result = transferService.transfer(request);

            // Then
            assertThat(result).isInstanceOf(TransactionResult.Success.class);
        }
    }

    @Nested
    @DisplayName("TransactionResult Tests")
    class TransactionResultTests {

        @Test
        @DisplayName("Should create Success result with message")
        void shouldCreateSuccessResultWithMessage() {
            // Given
            String message = "Transfer completed successfully";

            // When
            TransactionResult.Success result = new TransactionResult.Success(message);

            // Then
            assertThat(result.message()).isEqualTo(message);
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when Success message is null")
        void shouldThrowExceptionWhenSuccessMessageIsNull() {
            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> new TransactionResult.Success(null)
            );
        }

        @Test
        @DisplayName("Should create Failure result with message")
        void shouldCreateFailureResultWithMessage() {
            // Given
            String message = "Insufficient balance";

            // When
            TransactionResult.Failure result = new TransactionResult.Failure(message);

            // Then
            assertThat(result.message()).isEqualTo(message);
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when Failure message is null")
        void shouldThrowExceptionWhenFailureMessageIsNull() {
            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> new TransactionResult.Failure(null)
            );
        }

        @Test
        @DisplayName("Should create Pending result with message and authorizationId")
        void shouldCreatePendingResultWithMessageAndAuthorizationId() {
            // Given
            String message = "Transfer requires approval";
            UUID authorizationId = UUID.randomUUID();

            // When
            TransactionResult.Pending result = new TransactionResult.Pending(message, authorizationId);

            // Then
            assertThat(result.message()).isEqualTo(message);
            assertThat(result.authorizationId()).isEqualTo(authorizationId);
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when Pending message is null")
        void shouldThrowExceptionWhenPendingMessageIsNull() {
            // Given
            UUID authorizationId = UUID.randomUUID();

            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> new TransactionResult.Pending(null, authorizationId)
            );
        }

        @Test
        @DisplayName("Should throw exception when Pending authorizationId is null")
        void shouldThrowExceptionWhenPendingAuthorizationIdIsNull() {
            // Given
            String message = "Transfer requires approval";

            // Given/When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> new TransactionResult.Pending(message, null)
            );
        }
    }

    @Nested
    @DisplayName("Get Transfer History")
    class GetTransferHistoryTests {

        @Test
        @DisplayName("Should get transfer history for account")
        void shouldGetTransferHistoryForAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            TransferEntity transfer1 = new TransferEntity();
            TransferEntity transfer2 = new TransferEntity();

            given(transferRepository.findByFromAccountIdOrToAccountId(accountId, accountId))
                    .willReturn(java.util.List.of(transfer1, transfer2));

            // When
            Object result = transferService.getTransferHistory(accountId);

            // Then
            assertThat(result).isNotNull();
            verify(transferRepository).findByFromAccountIdOrToAccountId(accountId, accountId);
        }

        @Test
        @DisplayName("Should return empty list when no transfer history")
        void shouldReturnEmptyListWhenNoTransferHistory() {
            // Given
            UUID accountId = UUID.randomUUID();
            given(transferRepository.findByFromAccountIdOrToAccountId(accountId, accountId))
                    .willReturn(java.util.List.of());

            // When
            Object result = transferService.getTransferHistory(accountId);

            // Then
            assertThat(result).isNotNull();
        }
    }
}
