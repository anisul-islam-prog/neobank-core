package com.neobank.fraud;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for fraud analysis async execution and metrics.
 */
@Configuration
@EnableAsync
public class FraudAnalysisConfig {

    @Bean(name = "fraudAnalysisExecutor")
    public Executor fraudAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("fraud-analysis-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }

    /**
     * Custom Micrometer metrics binder for fraud detection.
     */
    @Bean
    public MeterBinder fraudMetricsBinder(MeterRegistry meterRegistry) {
        return registry -> {
            // These metrics are registered by FraudService
            // bank.fraud.detected.total - Counter for total fraud detected
            // bank.fraud.velocity.violations - Counter for velocity check violations
            // bank.fraud.blacklist.hits - Counter for blacklist hits
            // bank.fraud.suspicious.patterns - Counter for suspicious patterns detected
        };
    }
}
