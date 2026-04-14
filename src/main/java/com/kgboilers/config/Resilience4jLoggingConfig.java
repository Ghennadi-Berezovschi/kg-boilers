package com.kgboilers.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jLoggingConfig {

    private static final Logger log =
            LoggerFactory.getLogger(Resilience4jLoggingConfig.class);

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public Resilience4jLoggingConfig(RetryRegistry retryRegistry,
                                     CircuitBreakerRegistry circuitBreakerRegistry) {
        this.retryRegistry = retryRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void registerListeners() {

        log.info("Registering Resilience4j listeners...");

        // 🔁 Retry логирование (для всех retry)
        retryRegistry.getAllRetries().forEach(retry ->
                retry.getEventPublisher()
                        .onRetry(event -> log.warn(
                                "Retry attempt {} for '{}' due to: {}",
                                event.getNumberOfRetryAttempts(),
                                event.getName(),
                                event.getLastThrowable() != null
                                        ? event.getLastThrowable().getMessage()
                                        : "unknown error"
                        ))
        );

        // 🔌 CircuitBreaker логирование (для всех CB)
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb ->
                cb.getEventPublisher()
                        .onStateTransition(event -> log.warn(
                                "CircuitBreaker '{}' changed state from {} to {}",
                                event.getCircuitBreakerName(),
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()
                        ))
                        .onFailureRateExceeded(event -> log.error(
                                "CircuitBreaker '{}' failure rate exceeded: {}%",
                                event.getCircuitBreakerName(),
                                event.getFailureRate()
                        ))
        );

        log.info("Resilience4j listeners successfully registered");
    }
}