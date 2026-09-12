package br.com.nevvesdev.notificationhub.application.dto;

import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationTemplate;

import java.util.Map;

public record SendNotificationCommand(
        String recipient,
        NotificationChannel channel,
        NotificationTemplate template,
        Map<String, Object> payload
) {
    public SendNotificationCommand {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Recipient is required");
        }
        if (channel == null) {
            throw new IllegalArgumentException("Channel is required");
        }
        if (template == null) {
            throw new IllegalArgumentException("Template is required");
        }
        if (payload == null) {
            payload = Map.of();
        }
    }
}