package br.com.nevvesdev.notificationhub.application.port.out;

import br.com.nevvesdev.notificationhub.domain.model.Notification;

public interface DeadLetterPublisher {

    void publish(Notification notification);
}