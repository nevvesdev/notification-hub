package br.com.nevvesdev.notificationhub.domain.valueobject;

import br.com.nevvesdev.notificationhub.shared.exception.DomainException;

import java.util.UUID;

public record NotificationId(UUID value) {

    public NotificationId {
        if (value == null) {
            throw new DomainException("Notification ID cannot be null");
        }
    }

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    public static NotificationId of(UUID value) {
        return new NotificationId(value);
    }

    public static NotificationId of(String value) {
        try {
            return new NotificationId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid Notification ID format: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}