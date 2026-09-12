package br.com.nevvesdev.notificationhub.infrastructure.persistence.repository;

import br.com.nevvesdev.notificationhub.application.port.out.NotificationRepository;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationStatus;
import br.com.nevvesdev.notificationhub.domain.model.Notification;
import br.com.nevvesdev.notificationhub.domain.valueobject.NotificationId;
import br.com.nevvesdev.notificationhub.infrastructure.persistence.mapper.NotificationMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;

    public NotificationRepositoryAdapter(
            NotificationJpaRepository jpaRepository,
            NotificationMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(
                jpaRepository.save(mapper.toEntity(notification))
        );
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<Notification> findByRecipient(String recipient) {
        return jpaRepository.findByRecipient(recipient)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findByStatus(NotificationStatus status) {
        return jpaRepository.findByStatus(status.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}