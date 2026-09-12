package br.com.nevvesdev.notificationhub.application.port.out;

import br.com.nevvesdev.notificationhub.domain.model.Notification;
import br.com.nevvesdev.notificationhub.domain.valueobject.NotificationId;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationStatus;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(NotificationId id);

    List<Notification> findByRecipient(String recipient);

    List<Notification> findByStatus(NotificationStatus status);
}