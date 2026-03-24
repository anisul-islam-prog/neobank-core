package com.neobank.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Resilience4j configuration for Phase 7: Resilience & Fault Tolerance.
 * Configures Circuit Breakers, Retries, Bulkheads, and Rate Limiters.
 */
@Configuration
public class ResilienceConfig {

    @Value("${neobank.threadpool.critical.core-size:20}")
    private int criticalCoreSize;

    @Value("${neobank.threadpool.critical.max-size:50}")
    private int criticalMaxSize;

    @Value("${neobank.threadpool.critical.queue-capacity:100}")
    private int criticalQueueCapacity;

    @Value("${neobank.threadpool.non-critical.core-size:5}")
    private int nonCriticalCoreSize;

    @Value("${neobank.threadpool.non-critical.max-size:15}")
    private int nonCriticalMaxSize;

    @Value("${neobank.threadpool.non-critical.queue-capacity:50}")
    private int nonCriticalQueueCapacity;

    // =============================================================================
    // Circuit Breaker Registry
    // =============================================================================

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .recordExceptions(
                java.net.ConnectException.class,
                java.util.concurrent.TimeoutException.class,
                org.springframework.dao.CannotAcquireLockException.class
            )
            .build();

        return CircuitBreakerRegistry.of(defaultConfig);
    }

    @Bean
    public CircuitBreaker transferCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("transfer");
    }

    @Bean
    public CircuitBreaker authCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("auth");
    }

    @Bean
    public CircuitBreaker analyticsCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("analytics");
    }

    // =============================================================================
    // Retry Registry
    // =============================================================================

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig defaultConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .enableExponentialBackoff()
            .exponentialBackoffMultiplier(2)
            .maxWaitDuration(Duration.ofSeconds(10))
            .retryExceptions(
                java.net.ConnectException.class,
                java.util.concurrent.TimeoutException.class,
                org.springframework.dao.OptimisticLockingFailureException.class
            )
            .build();

        return RetryRegistry.of(defaultConfig);
    }

    @Bean
    public Retry transferRetry(RetryRegistry registry) {
        return registry.retry("transfer");
    }

    @Bean
    public Retry authRetry(RetryRegistry registry) {
        return registry.retry("auth");
    }

    // =============================================================================
    // Bulkhead Registry with Thread Pool Isolation
    // =============================================================================

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig defaultConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(100)
            .maxWaitDuration(Duration.ofSeconds(1))
            .build();

        return BulkheadRegistry.of(defaultConfig);
    }

    @Bean
    public Bulkhead criticalBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("critical");
    }

    @Bean
    public Bulkhead nonCriticalBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("non-critical");
    }

    // =============================================================================
    // Thread Pool Executors for Bulkhead Isolation
    // =============================================================================

    @Bean
    @Primary
    public Executor criticalExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(criticalCoreSize);
        executor.setMaxPoolSize(criticalMaxSize);
        executor.setQueueCapacity(criticalQueueCapacity);
        executor.setThreadNamePrefix("critical-executor-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor nonCriticalExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(nonCriticalCoreSize);
        executor.setMaxPoolSize(nonCriticalMaxSize);
        executor.setQueueCapacity(nonCriticalQueueCapacity);
        executor.setThreadNamePrefix("non-critical-executor-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    // =============================================================================
    // Rate Limiter Registry
    // =============================================================================

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
            .limitForPeriod(100)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ZERO)
            .build();

        return RateLimiterRegistry.of(defaultConfig);
    }

    @Bean
    public RateLimiter moduleCallsRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("module-calls");
    }
}
