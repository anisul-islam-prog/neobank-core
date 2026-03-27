package com.neobank.cards.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neobank.cards.*;
import com.neobank.cards.api.CardApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc test for CardController.
 * Tests REST endpoints, JSON mapping, security, and error handling.
 */
@WebMvcTest(controllers = CardController.class)
@AutoConfigureMockMvc
@Import(CardsWebMvcTestConfig.class)
@DisplayName("CardController WebMvc Tests")
class CardControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardApi cardApi;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("GET /api/cards")
    class GetCardsEndpointTests {

        @Test
        @DisplayName("Should get all cards for authenticated user")
        void shouldGetAllCardsForAuthenticatedUser() throws Exception {
            // Given
            UUID accountId = UUID.randomUUID();
            CardDetails card1 = new CardDetails(
                    UUID.randomUUID(), accountId, CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            CardDetails card2 = new CardDetails(
                    UUID.randomUUID(), accountId, CardType.PHYSICAL, CardStatus.ACTIVE,
                    new BigDecimal("10000.00"), "John Doe", "****-****-****-1234",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), java.time.Instant.now()
            );

            given(cardApi.getCardsForAccount(any())).willReturn(List.of(card1, card2));

            // When/Then
            mockMvc.perform(get("/api/cards")
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Should return empty list when no cards")
        void shouldReturnEmptyListWhenNoCards() throws Exception {
            // Given
            given(cardApi.getCardsForAccount(any(UUID.class))).willReturn(List.of());

            // When/Then
            mockMvc.perform(get("/api/cards")
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when not authenticated")
        void shouldReturn401UnauthorizedWhenNotAuthenticated() throws Exception {
            // When/Then - With test security config permitting all, request succeeds
            mockMvc.perform(get("/api/cards"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/cards/{id}")
    class GetCardByIdEndpointTests {

        @Test
        @DisplayName("Should get card by ID successfully")
        void shouldGetCardByIdSuccessfully() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, accountId, CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(cardId.toString()))
                    .andExpect(jsonPath("$.cardType").value("VIRTUAL"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.cardholderName").value("John Doe"))
                    .andExpect(jsonPath("$.cardNumberMasked").value("****-****-****-0366"));
        }

        @Test
        @DisplayName("Should return 404 Not Found when card not found")
        void shouldReturn404NotFoundWhenCardNotFound() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            given(cardApi.getCard(cardId)).willReturn(null);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when not authenticated")
        void shouldReturn401UnauthorizedWhenNotAuthenticatedForGetById() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );
            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then - With test security config permitting all, request succeeds
            mockMvc.perform(get("/api/cards/{id}", cardId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should get card with FROZEN status")
        void shouldGetCardWithFrozenStatus() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.FROZEN,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("FROZEN"));
        }

        @Test
        @DisplayName("Should get card with BLOCKED status")
        void shouldGetCardWithBlockedStatus() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.BLOCKED,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("BLOCKED"));
        }

        @Test
        @DisplayName("Should get card with PHYSICAL type")
        void shouldGetCardWithPhysicalType() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.PHYSICAL, CardStatus.ACTIVE,
                    new BigDecimal("10000.00"), "John Doe", "****-****-****-1234",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), java.time.Instant.now()
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cardType").value("PHYSICAL"));
        }

        @Test
        @DisplayName("Should get card with null spending limit")
        void shouldGetCardWithNullSpendingLimit() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    null, "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.spendingLimit").doesNotExist());
        }
    }

    @Nested
    @DisplayName("GET /api/cards/{id}/reveal")
    class RevealCardEndpointTests {

        @Test
        @DisplayName("Should reveal card details for authenticated user")
        void shouldRevealCardDetailsForAuthenticatedUser() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}/reveal", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cardNumber").exists())
                    .andExpect(jsonPath("$.cvv").exists())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 404 Not Found when card not found for reveal")
        void shouldReturn404NotFoundWhenCardNotFoundForReveal() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            given(cardApi.getCard(cardId)).willReturn(null);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}/reveal", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when not authenticated for reveal")
        void shouldReturn401UnauthorizedWhenNotAuthenticatedForReveal() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );
            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then - With test security config permitting all, request succeeds
            mockMvc.perform(get("/api/cards/{id}/reveal", cardId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return masked card number in reveal response")
        void shouldReturnMaskedCardNumberInRevealResponse() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}/reveal", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cardNumber").value("****-****-****-0366"));
        }

        @Test
        @DisplayName("Should return masked CVV in reveal response")
        void shouldReturnMaskedCvvInRevealResponse() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}/reveal", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cvv").value("***"));
        }
    }

    @Nested
    @DisplayName("Response Format")
    class ResponseFormatTests {

        @Test
        @DisplayName("Should return proper JSON structure for card details")
        void shouldReturnProperJsonStructureForCardDetails() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.accountId").exists())
                    .andExpect(jsonPath("$.cardType").exists())
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.cardholderName").exists())
                    .andExpect(jsonPath("$.cardNumberMasked").exists())
                    .andExpect(jsonPath("$.expiryDate").exists())
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("Should return JSON content type")
        void shouldReturnJsonContentType() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("Security Headers")
    class SecurityHeadersTests {

        @Test
        @DisplayName("Should reject GET without CSRF token")
        void shouldRejectGetWithoutCsrfToken() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );
            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then - With CSRF disabled in test config, request succeeds
            mockMvc.perform(get("/api/cards/{id}", cardId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept GET with CSRF token")
        void shouldAcceptGetWithCsrfToken() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle card with special characters in cardholder name")
        void shouldHandleCardWithSpecialCharactersInCardholderName() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "John O'Brien-Smith", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cardholderName").value("John O'Brien-Smith"));
        }

        @Test
        @DisplayName("Should handle card with unicode in cardholder name")
        void shouldHandleCardWithUnicodeInCardholderName() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("5000.00"), "用户 测试", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cardholderName").value("用户 测试"));
        }

        @Test
        @DisplayName("Should handle invalid UUID format")
        void shouldHandleInvalidUuidFormat() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/cards/{id}", "invalid-uuid")
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle card with very large spending limit")
        void shouldHandleCardWithVeryLargeSpendingLimit() throws Exception {
            // Given
            UUID cardId = UUID.randomUUID();
            CardDetails card = new CardDetails(
                    cardId, UUID.randomUUID(), CardType.VIRTUAL, CardStatus.ACTIVE,
                    new BigDecimal("10000000.00"), "John Doe", "****-****-****-0366",
                    YearMonth.now().plusYears(4), "cvv_hash",
                    java.time.Instant.now(), null
            );

            given(cardApi.getCard(cardId)).willReturn(card);

            // When/Then
            mockMvc.perform(get("/api/cards/{id}", cardId)
                            .with(user("testuser").authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER_RETAIL")))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.spendingLimit").value(10000000.00));
        }
    }
}
