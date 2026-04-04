package com.neobank.gateway.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Reactive global filter that propagates traceId and spanId headers to downstream services
 * and adds X-Trace-Id to response headers for client-side error reporting.
 * 
 * Uses Spring WebFlux's ServerWebExchange for header manipulation.
 */
@Component
public class TracePropagationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(TracePropagationFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final String SAMPLED_HEADER = "X-Sampled";

    private final Optional<Tracer> tracer;

    public TracePropagationFilter(Optional<Tracer> tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return tracer.map(t -> {
                Span currentSpan = t.currentSpan();

                if (currentSpan != null && currentSpan.context() != null) {
                    String traceId = currentSpan.context().traceId();
                    String spanId = currentSpan.context().spanId();

                    log.debug("Trace propagated - TraceId: {}, SpanId: {} for request: {}",
                        traceId, spanId, exchange.getRequest().getURI().getPath());

                    HttpHeaders headers = exchange.getResponse().getHeaders();
                    headers.set(TRACE_ID_HEADER, traceId);
                    headers.set(SPAN_ID_HEADER, spanId);
                    headers.set(SAMPLED_HEADER, String.valueOf(currentSpan.context().sampled()));

                    return chain.filter(exchange);
                } else {
                    log.debug("No active span found for request: {}", exchange.getRequest().getURI().getPath());
                    return chain.filter(exchange);
                }
            })
            .orElseGet(() -> chain.filter(exchange));
    }
}
