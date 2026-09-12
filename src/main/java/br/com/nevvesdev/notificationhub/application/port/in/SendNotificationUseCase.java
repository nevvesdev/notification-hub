package br.com.nevvesdev.notificationhub.application.port.in;

import br.com.nevvesdev.notificationhub.application.dto.SendNotificationCommand;
import br.com.nevvesdev.notificationhub.application.dto.NotificationResponse;

public interface SendNotificationUseCase {

    NotificationResponse execute(SendNotificationCommand command);
}