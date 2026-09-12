package br.com.nevvesdev.notificationhub.application.port.in;

import br.com.nevvesdev.notificationhub.application.dto.NotificationResponse;

import java.util.List;
import java.util.Optional;

public interface QueryNotificationUseCase {

    Optional<NotificationResponse> findById(String id);

    List<NotificationResponse> findByRecipient(String recipient);

    List<NotificationResponse> findPending();
}