package br.com.nevvesdev.notificationhub.domain.model;

import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationStatus;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationTemplate;
import br.com.nevvesdev.notificationhub.domain.exception.NotificationAlreadySentException;
import br.com.nevvesdev.notificationhub.domain.valueobject.NotificationId;
import br.com.nevvesdev.notificationhub.domain.valueobject.Recipient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Notification {

    private static final int MAX_ATTEMPTS = 3;

    private final NotificationId id;
    private final Recipient recipient;
    private final NotificationChannel channel;
    private final NotificationTemplate template;
    private final Map<String, Object> payload;
    private NotificationStatus status;
    private final List<NotificationAttempt> attempts;
    private final Instant createdAt;
    private Instant updatedAt;

    private Notification(
            NotificationId id,
            Recipient recipient,
            NotificationChannel channel,
            NotificationTemplate template,
            Map<String, Object> payload
    ) {
        this.id = id;
        this.recipient = recipient;
        this.channel = channel;
        this.template = template;
        this.payload = Map.copyOf(payload);
        this.status = NotificationStatus.PENDING;
        this.attempts = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Notification create(
            Recipient recipient,
            NotificationChannel channel,
            NotificationTemplate template,
            Map<String, Object> payload
    ) {
        return new Notification(
                NotificationId.generate(),
                recipient,
                channel,
                template,
                payload
        );
    }

    // Reconstitui a entidade a partir da persistência (sem gerar novo ID)
    public static Notification reconstitute(
            NotificationId id,
            Recipient recipient,
            NotificationChannel channel,
            NotificationTemplate template,
            Map<String, Object> payload,
            NotificationStatus status,
            List<NotificationAttempt> attempts,
            Instant createdAt,
            Instant updatedAt
    ) {
        Notification notification = new Notification(id, recipient, channel, template, payload);
        notification.status = status;
        notification.attempts.addAll(attempts);
        return notification;
    }

    public void markAsProcessing() {
        guardAgainstTerminalStatus();
        this.status = NotificationStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void recordSuccess() {
        guardAgainstTerminalStatus();
        this.attempts.add(NotificationAttempt.success(attempts.size() + 1));
        this.status = NotificationStatus.SENT;
        this.updatedAt = Instant.now();
    }

    public void recordFailure(String errorMessage) {
        guardAgainstTerminalStatus();
        this.attempts.add(NotificationAttempt.failure(attempts.size() + 1, errorMessage));
        this.updatedAt = Instant.now();

        if (attempts.size() >= MAX_ATTEMPTS) {
            this.status = NotificationStatus.DEAD_LETTERED;
        } else {
            this.status = NotificationStatus.FAILED;
        }
    }

    public boolean hasExhaustedAttempts() {
        return attempts.size() >= MAX_ATTEMPTS;
    }

    public boolean isRetryable() {
        return status.isRetryable() && !hasExhaustedAttempts();
    }

    private void guardAgainstTerminalStatus() {
        if (status.isTerminal()) {
            throw new NotificationAlreadySentException(id.toString());
        }
    }

    // Getters
    public NotificationId getId() { return id; }
    public Recipient getRecipient() { return recipient; }
    public NotificationChannel getChannel() { return channel; }
    public NotificationTemplate getTemplate() { return template; }
    public Map<String, Object> getPayload() { return payload; }
    public NotificationStatus getStatus() { return status; }
    public List<NotificationAttempt> getAttempts() { return Collections.unmodifiableList(attempts); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public int getAttemptCount() { return attempts.size(); }
}