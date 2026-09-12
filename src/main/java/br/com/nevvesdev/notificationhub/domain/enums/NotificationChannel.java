package br.com.nevvesdev.notificationhub.domain.enums;

public enum NotificationChannel {
    EMAIL,
    SMS,
    PUSH;

    public boolean isSupported() {
        return this == EMAIL || this == SMS || this == PUSH;
    }
}
