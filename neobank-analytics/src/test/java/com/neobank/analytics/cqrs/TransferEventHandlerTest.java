package com.neobank.analytics.cqrs;

import com.neobank.core.transfers.MoneyTransferredEvent;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransferEventHandler using JUnit 5 and Mockito.
 * Tests event handling and BI table population.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransferEventHandler Unit Tests")
class TransferEventHandlerTest {

    @Mock
    private BiTransactionHistoryRepository biRepository;

    private TransferEventHandler eventHandler;

    @BeforeEach
    void setUp() {
        eventHandler = new TransferEventHandler(biRepository);
    }

    @Nested
    @DisplayName("Event Handling")
    class EventHandlingTests {

        @Test
        @DisplayName("Should process MoneyTransferredEvent and save BI record")
        void shouldProcessMoneyTransferredEventAndSaveBiRecord() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            String occurredAt = Instant.now().toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getTransferId()).isEqualTo(transferId);
            assertThat(savedBi.getFromAccountId()).isEqualTo(senderId);
            assertThat(savedBi.getToAccountId()).isEqualTo(receiverId);
            assertThat(savedBi.getAmount()).isEqualByComparingTo(amount);
            assertThat(savedBi.getCurrency()).isEqualTo(currency);
            assertThat(savedBi.getStatus()).isEqualTo("COMPLETED");
            assertThat(savedBi.getTransactionType()).isEqualTo("TRANSFER");
            assertThat(savedBi.getChannel()).isEqualTo("CORE_BANKING");
            assertThat(savedBi.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should handle null currency and default to USD")
        void shouldHandleNullCurrencyAndDefaultToUsd() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = null;
            String occurredAt = Instant.now().toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should set processedAt timestamp")
        void shouldSetProcessedAtTimestamp() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            String occurredAt = Instant.now().toString();
            Instant beforeProcessing = Instant.now();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getProcessedAt()).isNotNull();
            assertThat(savedBi.getProcessedAt()).isAfterOrEqualTo(beforeProcessing);
        }

        @Test
        @DisplayName("Should parse occurredAt timestamp correctly")
        void shouldParseOccurredAtTimestampCorrectly() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            Instant expectedInstant = Instant.now();
            String occurredAt = expectedInstant.toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getOccurredAt()).isEqualTo(expectedInstant);
        }

        @Test
        @DisplayName("Should handle repository save exception gracefully")
        void shouldHandleRepositorySaveExceptionGracefully() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            String occurredAt = Instant.now().toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            doThrow(new RuntimeException("Database error"))
                    .when(biRepository).save(any(BiTransactionHistory.class));

            // When/Then
            assertThatThrownBy(() -> eventHandler.onMoneyTransferred(event))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");
        }
    }

    @Nested
    @DisplayName("Event Validation")
    class EventValidationTests {

        @Test
        @DisplayName("Should handle null transfer ID")
        void shouldHandleNullTransferId() {
            // Given/When/Then
            assertThatThrownBy(() -> new MoneyTransferredEvent(
                    null, UUID.randomUUID(), UUID.randomUUID(),
                    new BigDecimal("100.00"), "USD", Instant.now().toString()
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("transferId must not be null");
        }

        @Test
        @DisplayName("Should handle null sender ID")
        void shouldHandleNullSenderId() {
            // Given/When/Then
            assertThatThrownBy(() -> new MoneyTransferredEvent(
                    UUID.randomUUID(), null, UUID.randomUUID(),
                    new BigDecimal("100.00"), "USD", Instant.now().toString()
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("senderId must not be null");
        }

        @Test
        @DisplayName("Should handle null receiver ID")
        void shouldHandleNullReceiverId() {
            // Given/When/Then
            assertThatThrownBy(() -> new MoneyTransferredEvent(
                    UUID.randomUUID(), UUID.randomUUID(), null,
                    new BigDecimal("100.00"), "USD", Instant.now().toString()
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("receiverId must not be null");
        }

        @Test
        @DisplayName("Should handle null amount")
        void shouldHandleNullAmount() {
            // Given/When/Then
            assertThatThrownBy(() -> new MoneyTransferredEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    null, "USD", Instant.now().toString()
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount must not be null");
        }

        @Test
        @DisplayName("Should handle null currency")
        void shouldHandleNullCurrency() {
            // Given/When/Then
            assertThatThrownBy(() -> new MoneyTransferredEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    new BigDecimal("100.00"), null, Instant.now().toString()
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("currency must not be null");
        }

        @Test
        @DisplayName("Should handle null occurredAt")
        void shouldHandleNullOccurredAt() {
            // Given/When/Then
            assertThatThrownBy(() -> new MoneyTransferredEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    new BigDecimal("100.00"), "USD", null
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("occurredAt must not be null");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero amount transfer")
        void shouldHandleZeroAmountTransfer() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.ZERO;
            String currency = "USD";
            String occurredAt = Instant.now().toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle very large amount transfer")
        void shouldHandleVeryLargeAmountTransfer() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("10000000.00");
            String currency = "USD";
            String occurredAt = Instant.now().toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getAmount()).isEqualByComparingTo(new BigDecimal("10000000.00"));
        }

        @Test
        @DisplayName("Should handle amount with 4 decimal places")
        void shouldHandleAmountWith4DecimalPlaces() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("123.4567");
            String currency = "USD";
            String occurredAt = Instant.now().toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getAmount()).isEqualByComparingTo(new BigDecimal("123.4567"));
        }

        @Test
        @DisplayName("Should handle same sender and receiver")
        void shouldHandleSameSenderAndReceiver() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");
            String currency = "USD";
            String occurredAt = Instant.now().toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, accountId, accountId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getFromAccountId()).isEqualTo(accountId);
            assertThat(savedBi.getToAccountId()).isEqualTo(accountId);
        }

        @Test
        @DisplayName("Should handle different currencies")
        void shouldHandleDifferentCurrencies() {
            // Given
            String[] currencies = {"USD", "EUR", "GBP", "JPY", "CHF"};

            for (String currency : currencies) {
                UUID transferId = UUID.randomUUID();
                UUID senderId = UUID.randomUUID();
                UUID receiverId = UUID.randomUUID();
                BigDecimal amount = new BigDecimal("500.00");
                String occurredAt = Instant.now().toString();

                MoneyTransferredEvent event = new MoneyTransferredEvent(
                        transferId, senderId, receiverId, amount, currency, occurredAt
                );

                // When
                eventHandler.onMoneyTransferred(event);

                // Then
                ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
                verify(biRepository, atLeastOnce()).save(biCaptor.capture());

                BiTransactionHistory savedBi = biCaptor.getValue();
                assertThat(savedBi.getCurrency()).isEqualTo(currency);
            }
        }

        @Test
        @DisplayName("Should handle past occurredAt timestamp")
        void shouldHandlePastOccurredAtTimestamp() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            Instant pastInstant = Instant.now().minusSeconds(86400); // 1 day ago
            String occurredAt = pastInstant.toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getOccurredAt()).isEqualTo(pastInstant);
        }

        @Test
        @DisplayName("Should handle future occurredAt timestamp")
        void shouldHandleFutureOccurredAtTimestamp() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            Instant futureInstant = Instant.now().plusSeconds(3600); // 1 hour in future
            String occurredAt = futureInstant.toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getOccurredAt()).isEqualTo(futureInstant);
        }

        @Test
        @DisplayName("Should handle multiple events in sequence")
        void shouldHandleMultipleEventsInSequence() {
            // Given
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();

            MoneyTransferredEvent event1 = new MoneyTransferredEvent(
                    UUID.randomUUID(), senderId, receiverId,
                    new BigDecimal("100.00"), "USD", Instant.now().toString()
            );
            MoneyTransferredEvent event2 = new MoneyTransferredEvent(
                    UUID.randomUUID(), senderId, receiverId,
                    new BigDecimal("200.00"), "USD", Instant.now().toString()
            );
            MoneyTransferredEvent event3 = new MoneyTransferredEvent(
                    UUID.randomUUID(), senderId, receiverId,
                    new BigDecimal("300.00"), "USD", Instant.now().toString()
            );

            // When
            eventHandler.onMoneyTransferred(event1);
            eventHandler.onMoneyTransferred(event2);
            eventHandler.onMoneyTransferred(event3);

            // Then
            verify(biRepository, times(3)).save(any(BiTransactionHistory.class));
        }

        @Test
        @DisplayName("Should handle negative amount")
        void shouldHandleNegativeAmount() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("-100.00");
            String currency = "USD";
            String occurredAt = Instant.now().toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getAmount()).isEqualByComparingTo(new BigDecimal("-100.00"));
        }
    }

    @Nested
    @DisplayName("BI Record Fields")
    class BiRecordFieldsTests {

        @Test
        @DisplayName("Should set all required fields")
        void shouldSetAllRequiredFields() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            String occurredAt = Instant.now().toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getId()).isNotNull();
            assertThat(savedBi.getTransferId()).isEqualTo(transferId);
            assertThat(savedBi.getFromAccountId()).isEqualTo(senderId);
            assertThat(savedBi.getToAccountId()).isEqualTo(receiverId);
            assertThat(savedBi.getAmount()).isEqualByComparingTo(amount);
            assertThat(savedBi.getCurrency()).isEqualTo(currency);
            assertThat(savedBi.getStatus()).isEqualTo("COMPLETED");
            assertThat(savedBi.getOccurredAt()).isEqualTo(Instant.parse(occurredAt));
            assertThat(savedBi.getProcessedAt()).isNotNull();
            assertThat(savedBi.getTransactionType()).isEqualTo("TRANSFER");
            assertThat(savedBi.getChannel()).isEqualTo("CORE_BANKING");
        }

        @Test
        @DisplayName("Should set optional fields to null")
        void shouldSetOptionalFieldsToNull() {
            // Given
            UUID transferId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");
            String currency = "USD";
            String occurredAt = Instant.now().toString();

            MoneyTransferredEvent event = new MoneyTransferredEvent(
                    transferId, senderId, receiverId, amount, currency, occurredAt
            );

            // When
            eventHandler.onMoneyTransferred(event);

            // Then
            ArgumentCaptor<BiTransactionHistory> biCaptor = ArgumentCaptor.forClass(BiTransactionHistory.class);
            verify(biRepository).save(biCaptor.capture());

            BiTransactionHistory savedBi = biCaptor.getValue();
            assertThat(savedBi.getFromOwnerName()).isNull();
            assertThat(savedBi.getToOwnerName()).isNull();
            assertThat(savedBi.getFromBalanceBefore()).isNull();
            assertThat(savedBi.getFromBalanceAfter()).isNull();
            assertThat(savedBi.getToBalanceBefore()).isNull();
            assertThat(savedBi.getToBalanceAfter()).isNull();
            assertThat(savedBi.getMetadata()).isNull();
        }
    }
}
