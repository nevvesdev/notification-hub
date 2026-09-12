package br.com.nevvesdev.notificationhub.infrastructure.kafka.dto;

import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationTemplate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationEventDTO(
        String recipient,
        NotificationChannel channel,
        NotificationTemplate template,
        Map<String, Object> payload
) {}