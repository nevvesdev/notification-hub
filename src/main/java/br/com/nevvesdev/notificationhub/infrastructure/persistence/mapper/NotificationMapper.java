package br.com.nevvesdev.notificationhub.infrastructure.persistence.mapper;

import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationStatus;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationTemplate;
import br.com.nevvesdev.notificationhub.domain.model.Notification;
import br.com.nevvesdev.notificationhub.domain.valueobject.NotificationId;
import br.com.nevvesdev.notificationhub.domain.valueobject.Recipient;
import br.com.nevvesdev.notificationhub.infrastructure.persistence.entity.NotificationJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class NotificationMapper {

    public NotificationJpaEntity toEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();

        entity.setId(notification.getId().value());
        entity.setRecipient(notification.getRecipient().toString());
        entity.setChannel(notification.getChannel().name());
        entity.setTemplate(notification.getTemplate().name());
        entity.setSubject(notification.getTemplate().getDefaultSubject());
        entity.setStatus(notification.getStatus().name());
        entity.setAttempts(notification.getAttemptCount());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setUpdatedAt(notification.getUpdatedAt());

        notification.getAttempts().stream()
                .filter(a -> !a.isSuccessful())
                .reduce((first, second) -> second)
                .ifPresent(last -> entity.setError(last.getErrorMessage()));

        return entity;
    }

    public Notification toDomain(NotificationJpaEntity entity) {
        return Notification.reconstitute(
                NotificationId.of(entity.getId()),
                Recipient.of(entity.getRecipient()),
                NotificationChannel.valueOf(entity.getChannel()),
                NotificationTemplate.valueOf(entity.getTemplate()),
                Map.of(),
                NotificationStatus.valueOf(entity.getStatus()),
                Collections.emptyList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}