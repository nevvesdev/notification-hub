package br.com.nevvesdev.notificationhub.domain.model;

import java.time.Instant;

public class NotificationAttempt {

    private final int attemptNumber;
    private final Instant occurredAt;
    private final boolean successful;
    private final String errorMessage;

    private NotificationAttempt(int attemptNumber, boolean successful, String errorMessage) {
        this.attemptNumber = attemptNumber;
        this.occurredAt = Instant.now();
        this.successful = successful;
        this.errorMessage = errorMessage;
    }

    public static NotificationAttempt success(int attemptNumber) {
        return new NotificationAttempt(attemptNumber, true, null);
    }

    public static NotificationAttempt failure(int attemptNumber, String errorMessage) {
        return new NotificationAttempt(attemptNumber, false, errorMessage);
    }

    public int getAttemptNumber() { return attemptNumber; }
    public Instant getOccurredAt() { return occurredAt; }
    public boolean isSuccessful() { return successful; }
    public String getErrorMessage() { return errorMessage; }
}