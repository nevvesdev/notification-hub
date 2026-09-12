package br.com.nevvesdev.notificationhub.infrastructure.persistence.repository;

import br.com.nevvesdev.notificationhub.infrastructure.persistence.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByRecipient(String recipient);

    List<NotificationJpaEntity> findByStatus(String status);
}