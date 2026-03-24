package com.neobank.gateway.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Global filter that propagates traceId and spanId headers to downstream services
 * and adds X-Trace-Id to response headers for client-side error reporting.
 */
@Component
public class TracePropagationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TracePropagationFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final String SAMPLED_HEADER = "X-Sampled";

    private final Optional<Tracer> tracer;

    public TracePropagationFilter(Optional<Tracer> tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Get current span from tracer if available
        tracer.ifPresent(t -> {
            Span currentSpan = t.currentSpan();

            if (currentSpan != null) {
                String traceId = currentSpan.context().traceId();
                String spanId = currentSpan.context().spanId();

                // Add trace headers to response for client-side error reporting
                response.setHeader(TRACE_ID_HEADER, traceId);
                response.setHeader(SPAN_ID_HEADER, spanId);
                response.setHeader(SAMPLED_HEADER, String.valueOf(currentSpan.context().sampled()));

                // Log trace information for debugging
                log.debug("Trace propagated - TraceId: {}, SpanId: {}", traceId, spanId);
            } else {
                log.debug("No active span found for request: {}", request.getRequestURI());
            }
        });

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
