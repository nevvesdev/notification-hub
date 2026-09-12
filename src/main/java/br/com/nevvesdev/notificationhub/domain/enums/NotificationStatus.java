package br.com.nevvesdev.notificationhub.domain.enums;

public enum NotificationStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    DEAD_LETTERED;

    public boolean isTerminal() {
        return this == SENT || this == DEAD_LETTERED;
    }

    public boolean isRetryable() {
        return this == FAILED;
    }
}