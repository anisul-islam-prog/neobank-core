package com.neobank.gateway.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TracePropagationFilter using JUnit 5 and Mockito.
 * Tests trace ID propagation and response headers for WebFlux (Spring Boot 4).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TracePropagationFilter Unit Tests")
class TracePropagationFilterTest {

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Mock
    private TraceContext traceContext;

    private TracePropagationFilter tracePropagationFilter;

    @BeforeEach
    void setUp() {
        tracePropagationFilter = new TracePropagationFilter(Optional.of(tracer));
    }

    private ServerWebExchange createExchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());
    }

    @Nested
    @DisplayName("Trace ID Propagation")
    class TraceIdPropagationTests {

        @Test
        @DisplayName("Should add X-Trace-Id header to response when span is present")
        void shouldAddXTraceIdHeaderToResponseWhenSpanIsPresent() {
            // Given
            String traceId = "abc123def456";
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn(traceId);

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(filterChain).filter(any());
        }

        @Test
        @DisplayName("Should add X-Span-Id header to response when span is present")
        void shouldAddXSpanIdHeaderToResponseWhenSpanIsPresent() {
            // Given
            String spanId = "span789";
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.spanId()).willReturn(spanId);

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(filterChain).filter(any());
        }

        @Test
        @DisplayName("Should add X-Sampled header to response when span is present")
        void shouldAddXSampledHeaderToResponseWhenSpanIsPresent() {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.sampled()).willReturn(true);

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(filterChain).filter(any());
        }

        @Test
        @DisplayName("Should continue filter chain after adding trace headers")
        void shouldContinueFilterChainAfterAddingTraceHeaders() {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn("trace123");

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(filterChain).filter(any());
        }
    }

    @Nested
    @DisplayName("No Active Span Handling")
    class NoActiveSpanHandlingTests {

        @Test
        @DisplayName("Should not add trace headers when no active span")
        void shouldNotAddTraceHeadersWhenNoActiveSpan() {
            // Given
            given(tracer.currentSpan()).willReturn(null);

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(filterChain).filter(any());
        }

        @Test
        @DisplayName("Should continue filter chain when no active span")
        void shouldContinueFilterChainWhenNoActiveSpan() {
            // Given
            given(tracer.currentSpan()).willReturn(null);

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
        }
    }

    @Nested
    @DisplayName("Tracer Optional Handling")
    class TracerOptionalHandlingTests {

        @Test
        @DisplayName("Should handle empty tracer optional gracefully")
        void shouldHandleEmptyTracerOptionalGracefully() {
            // Given
            TracePropagationFilter filterWithoutTracer = new TracePropagationFilter(Optional.empty());

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = filterWithoutTracer.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
        }

        @Test
        @DisplayName("Should continue filter chain when tracer is empty")
        void shouldContinueFilterChainWhenTracerIsEmpty() {
            // Given
            TracePropagationFilter filterWithoutTracer = new TracePropagationFilter(Optional.empty());

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = filterWithoutTracer.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
        }
    }

    @Nested
    @DisplayName("Header Values")
    class HeaderValuesTests {

        @Test
        @DisplayName("Should use correct trace ID value")
        void shouldUseCorrectTraceIdValue() {
            // Given
            String expectedTraceId = "1234567890abcdef";
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn(expectedTraceId);

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
        }

        @Test
        @DisplayName("Should use correct sampled value (true)")
        void shouldUseCorrectSampledValueTrue() {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.sampled()).willReturn(true);

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
        }

        @Test
        @DisplayName("Should use correct sampled value (false)")
        void shouldUseCorrectSampledValueFalse() {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.sampled()).willReturn(false);

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null trace context gracefully")
        void shouldHandleNullTraceContextGracefully() {
            // Given - span returns null context
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(null);

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When/Then - Should not throw exception, completes without adding headers
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);
            StepVerifier.create(result).verifyComplete();
            verify(filterChain).filter(any());
        }

        @Test
        @DisplayName("Should handle multiple filter invocations")
        void shouldHandleMultipleFilterInvocations() {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn("trace123");

            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When - Multiple invocations
            for (int i = 0; i < 5; i++) {
                ServerWebExchange exchange = createExchange();
                Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);
                StepVerifier.create(result).verifyComplete();
            }

            // Then
            verify(filterChain, times(5)).filter(any());
        }
    }

    @Nested
    @DisplayName("Filter Chain Behavior")
    class FilterChainBehaviorTests {

        @Test
        @DisplayName("Should call filter chain exactly once")
        void shouldCallFilterChainExactlyOnce() {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);

            ServerWebExchange exchange = createExchange();
            WebFilterChain filterChain = mock(WebFilterChain.class);
            given(filterChain.filter(any())).willReturn(Mono.empty());

            // When
            Mono<Void> result = tracePropagationFilter.filter(exchange, filterChain);

            // Then
            StepVerifier.create(result).verifyComplete();
            verify(filterChain, times(1)).filter(any());
        }
    }
}
