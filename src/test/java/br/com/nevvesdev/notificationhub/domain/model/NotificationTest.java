package br.com.nevvesdev.notificationhub.domain.model;

import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationStatus;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationTemplate;
import br.com.nevvesdev.notificationhub.domain.exception.NotificationAlreadySentException;
import br.com.nevvesdev.notificationhub.domain.valueobject.Recipient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Notification Entity")
class NotificationTest {

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.create(
                Recipient.of("joao@nevvesdev.com.br"),
                NotificationChannel.EMAIL,
                NotificationTemplate.WELCOME,
                Map.of("name", "João")
        );
    }

    @Test
    @DisplayName("should start with PENDING status")
    void shouldStartPending() {
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getAttemptCount()).isZero();
    }

    @Test
    @DisplayName("should transition to PROCESSING")
    void shouldTransitionToProcessing() {
        notification.markAsProcessing();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PROCESSING);
    }

    @Test
    @DisplayName("should transition to SENT after success")
    void shouldTransitionToSent() {
        notification.markAsProcessing();
        notification.recordSuccess();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getAttemptCount()).isEqualTo(1);
        assertThat(notification.getStatus().isTerminal()).isTrue();
    }

    @Test
    @DisplayName("should transition to FAILED after first failure")
    void shouldTransitionToFailed() {
        notification.markAsProcessing();
        notification.recordFailure("SMTP connection refused");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getAttemptCount()).isEqualTo(1);
        assertThat(notification.isRetryable()).isTrue();
    }

    @Test
    @DisplayName("should transition to DEAD_LETTERED after max attempts")
    void shouldTransitionToDeadLettered() {
        notification.markAsProcessing();
        notification.recordFailure("error 1");
        notification.recordFailure("error 2");
        notification.recordFailure("error 3");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DEAD_LETTERED);
        assertThat(notification.getAttemptCount()).isEqualTo(3);
        assertThat(notification.hasExhaustedAttempts()).isTrue();
        assertThat(notification.isRetryable()).isFalse();
    }

    @Test
    @DisplayName("should guard against reprocessing a sent notification")
    void shouldGuardAgainstTerminalStatus() {
        notification.markAsProcessing();
        notification.recordSuccess();

        assertThatThrownBy(() -> notification.markAsProcessing())
                .isInstanceOf(NotificationAlreadySentException.class);
    }

    @Test
    @DisplayName("should keep payload immutable")
    void shouldKeepPayloadImmutable() {
        Map<String, Object> payload = notification.getPayload();

        assertThatThrownBy(() -> payload.put("extra", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should generate unique IDs for each notification")
    void shouldGenerateUniqueIds() {
        Notification other = Notification.create(
                Recipient.of("outro@nevvesdev.com.br"),
                NotificationChannel.EMAIL,
                NotificationTemplate.GENERIC,
                Map.of()
        );

        assertThat(notification.getId()).isNotEqualTo(other.getId());
    }
}