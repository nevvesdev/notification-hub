package br.com.nevvesdev.notificationhub.application.dto;

import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationStatus;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationTemplate;
import br.com.nevvesdev.notificationhub.domain.model.Notification;

import java.time.Instant;

public record NotificationResponse(
        String id,
        String recipient,
        NotificationChannel channel,
        NotificationTemplate template,
        NotificationStatus status,
        int attemptCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId().toString(),
                notification.getRecipient().toString(),
                notification.getChannel(),
                notification.getTemplate(),
                notification.getStatus(),
                notification.getAttemptCount(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}