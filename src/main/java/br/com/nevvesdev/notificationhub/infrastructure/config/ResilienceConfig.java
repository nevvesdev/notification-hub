package br.com.nevvesdev.notificationhub.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

    public ResilienceConfig(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry
    ) {
        circuitBreakerRegistry.circuitBreaker("emailSender")
                .getEventPublisher()
                .onStateTransition(event -> log.warn(
                        "CircuitBreaker [emailSender] state transition: {} → {}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()
                ))
                .onCallNotPermitted(event -> log.error(
                        "CircuitBreaker [emailSender] OPEN — call rejected"
                ));

        retryRegistry.retry("emailSender")
                .getEventPublisher()
                .onRetry(event -> log.warn(
                        "Retry [emailSender] attempt={} reason={}",
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable() != null
                                ? event.getLastThrowable().getMessage()
                                : "unknown"
                ))
                .onSuccess(event -> log.info(
                        "Retry [emailSender] succeeded after {} attempt(s)",
                        event.getNumberOfRetryAttempts()
                ))
                .onError(event -> log.error(
                        "Retry [emailSender] exhausted after {} attempt(s) — final error: {}",
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable() != null
                                ? event.getLastThrowable().getMessage()
                                : "unknown"
                ));
    }
}