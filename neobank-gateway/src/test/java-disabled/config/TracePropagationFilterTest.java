package com.neobank.gateway.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TracePropagationFilter using JUnit 5 and Mockito.
 * Tests trace ID propagation and response headers.
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

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private TracePropagationFilter tracePropagationFilter;

    @BeforeEach
    void setUp() {
        tracePropagationFilter = new TracePropagationFilter(Optional.of(tracer));
    }

    @Nested
    @DisplayName("Trace ID Propagation")
    class TraceIdPropagationTests {

        @Test
        @DisplayName("Should add X-Trace-Id header to response when span is present")
        void shouldAddXTraceIdHeaderToResponseWhenSpanIsPresent() throws Exception {
            // Given
            String traceId = "abc123def456";
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn(traceId);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Trace-Id", traceId);
        }

        @Test
        @DisplayName("Should add X-Span-Id header to response when span is present")
        void shouldAddXSpanIdHeaderToResponseWhenSpanIsPresent() throws Exception {
            // Given
            String spanId = "span789";
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.spanId()).willReturn(spanId);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Span-Id", spanId);
        }

        @Test
        @DisplayName("Should add X-Sampled header to response when span is present")
        void shouldAddXSampledHeaderToResponseWhenSpanIsPresent() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.sampled()).willReturn(true);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Sampled", "true");
        }

        @Test
        @DisplayName("Should continue filter chain after adding trace headers")
        void shouldContinueFilterChainAfterAddingTraceHeaders() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn("trace123");

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("No Active Span Handling")
    class NoActiveSpanHandlingTests {

        @Test
        @DisplayName("Should not add trace headers when no active span")
        void shouldNotAddTraceHeadersWhenNoActiveSpan() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(null);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response, never()).setHeader(eq("X-Trace-Id"), anyString());
            verify(response, never()).setHeader(eq("X-Span-Id"), anyString());
            verify(response, never()).setHeader(eq("X-Sampled"), anyString());
        }

        @Test
        @DisplayName("Should continue filter chain when no active span")
        void shouldContinueFilterChainWhenNoActiveSpan() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(null);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should log debug message when no active span")
        void shouldLogDebugMessageWhenNoActiveSpan() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(null);
            given(request.getRequestURI()).willReturn("/api/test");

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then - Filter should complete without error
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Tracer Optional Handling")
    class TracerOptionalHandlingTests {

        @Test
        @DisplayName("Should handle empty tracer optional gracefully")
        void shouldHandleEmptyTracerOptionalGracefully() throws Exception {
            // Given
            TracePropagationFilter filterWithoutTracer = new TracePropagationFilter(Optional.empty());

            // When
            filterWithoutTracer.doFilterInternal(request, response, filterChain);

            // Then - Should not throw exception
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not add trace headers when tracer is empty")
        void shouldNotAddTraceHeadersWhenTracerIsEmpty() throws Exception {
            // Given
            TracePropagationFilter filterWithoutTracer = new TracePropagationFilter(Optional.empty());

            // When
            filterWithoutTracer.doFilterInternal(request, response, filterChain);

            // Then
            verify(response, never()).setHeader(eq("X-Trace-Id"), anyString());
        }

        @Test
        @DisplayName("Should continue filter chain when tracer is empty")
        void shouldContinueFilterChainWhenTracerIsEmpty() throws Exception {
            // Given
            TracePropagationFilter filterWithoutTracer = new TracePropagationFilter(Optional.empty());

            // When
            filterWithoutTracer.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Header Values")
    class HeaderValuesTests {

        @Test
        @DisplayName("Should use correct trace ID value")
        void shouldUseCorrectTraceIdValue() throws Exception {
            // Given
            String expectedTraceId = "1234567890abcdef";
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn(expectedTraceId);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Trace-Id", expectedTraceId);
        }

        @Test
        @DisplayName("Should use correct span ID value")
        void shouldUseCorrectSpanIdValue() throws Exception {
            // Given
            String expectedSpanId = "fedcba0987654321";
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.spanId()).willReturn(expectedSpanId);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Span-Id", expectedSpanId);
        }

        @Test
        @DisplayName("Should use correct sampled value (true)")
        void shouldUseCorrectSampledValueTrue() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.sampled()).willReturn(true);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Sampled", "true");
        }

        @Test
        @DisplayName("Should use correct sampled value (false)")
        void shouldUseCorrectSampledValueFalse() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.sampled()).willReturn(false);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Sampled", "false");
        }

        @Test
        @DisplayName("Should handle long trace ID")
        void shouldHandleLongTraceId() throws Exception {
            // Given
            String longTraceId = "a".repeat(64);
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn(longTraceId);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Trace-Id", longTraceId);
        }

        @Test
        @DisplayName("Should handle hexadecimal trace ID")
        void shouldHandleHexadecimalTraceId() throws Exception {
            // Given
            String hexTraceId = "abcdef1234567890abcdef1234567890";
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn(hexTraceId);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Trace-Id", hexTraceId);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null trace context gracefully")
        void shouldHandleNullTraceContextGracefully() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(null);

            // When/Then - Should not throw exception
            tracePropagationFilter.doFilterInternal(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle multiple filter invocations")
        void shouldHandleMultipleFilterInvocations() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn("trace123");

            // When - Multiple invocations
            for (int i = 0; i < 5; i++) {
                tracePropagationFilter.doFilterInternal(request, response, filterChain);
            }

            // Then
            verify(filterChain, times(5)).doFilter(request, response);
            verify(response, times(5)).setHeader("X-Trace-Id", "trace123");
        }

        @Test
        @DisplayName("Should handle different trace IDs for different requests")
        void shouldHandleDifferentTraceIdsForDifferentRequests() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn("trace1").willReturn("trace2");

            // When - Two requests with different trace IDs
            tracePropagationFilter.doFilterInternal(request, response, filterChain);
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Trace-Id", "trace1");
            verify(response).setHeader("X-Trace-Id", "trace2");
        }

        @Test
        @DisplayName("Should handle empty trace ID")
        void shouldHandleEmptyTraceId() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn("");

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Trace-Id", "");
        }

        @Test
        @DisplayName("Should handle special characters in trace ID")
        void shouldHandleSpecialCharactersInTraceId() throws Exception {
            // Given
            String traceIdWithSpecialChars = "trace-123_abc.def";
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn(traceIdWithSpecialChars);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Trace-Id", traceIdWithSpecialChars);
        }
    }

    @Nested
    @DisplayName("Filter Chain Behavior")
    class FilterChainBehaviorTests {

        @Test
        @DisplayName("Should call filter chain exactly once")
        void shouldCallFilterChainExactlyOnce() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should add headers before calling filter chain")
        void shouldAddHeadersBeforeCallingFilterChain() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn("trace123");

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then - Verify header was set
            verify(response).setHeader("X-Trace-Id", "trace123");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should propagate ServletException from filter chain")
        void shouldPropagateServletExceptionFromFilterChain() throws Exception {
            // Given
            ServletException expectedException = new ServletException("Test exception");
            doThrow(expectedException).when(filterChain).doFilter(request, response);

            // When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                ServletException.class,
                () -> tracePropagationFilter.doFilterInternal(request, response, filterChain)
            );
        }

        @Test
        @DisplayName("Should propagate IOException from filter chain")
        void shouldPropagateIOExceptionFromFilterChain() throws Exception {
            // Given
            IOException expectedException = new IOException("Test exception");
            doThrow(expectedException).when(filterChain).doFilter(request, response);

            // When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                IOException.class,
                () -> tracePropagationFilter.doFilterInternal(request, response, filterChain)
            );
        }
    }

    @Nested
    @DisplayName("Header Constants")
    class HeaderConstantsTests {

        @Test
        @DisplayName("Should use correct X-Trace-Id header name")
        void shouldUseCorrectXTraceIdHeaderName() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.traceId()).willReturn("trace123");

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Trace-Id", "trace123");
        }

        @Test
        @DisplayName("Should use correct X-Span-Id header name")
        void shouldUseCorrectXSpanIdHeaderName() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.spanId()).willReturn("span123");

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Span-Id", "span123");
        }

        @Test
        @DisplayName("Should use correct X-Sampled header name")
        void shouldUseCorrectXSampledHeaderName() throws Exception {
            // Given
            given(tracer.currentSpan()).willReturn(span);
            given(span.context()).willReturn(traceContext);
            given(traceContext.sampled()).willReturn(true);

            // When
            tracePropagationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-Sampled", "true");
        }
    }
}
