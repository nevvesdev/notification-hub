package br.com.nevvesdev.notificationhub.application.port.out;

import br.com.nevvesdev.notificationhub.domain.model.Notification;

public interface NotificationSender {

    void send(Notification notification);

    boolean supports(br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel channel);
}