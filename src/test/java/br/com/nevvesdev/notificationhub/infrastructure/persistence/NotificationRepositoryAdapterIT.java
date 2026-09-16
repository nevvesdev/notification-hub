package br.com.nevvesdev.notificationhub.infrastructure.persistence;

import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationStatus;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationTemplate;
import br.com.nevvesdev.notificationhub.domain.model.Notification;
import br.com.nevvesdev.notificationhub.domain.valueobject.Recipient;
import br.com.nevvesdev.notificationhub.infrastructure.persistence.repository.NotificationRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("NotificationRepositoryAdapter Integration Test")
class NotificationRepositoryAdapterIT {

    @Autowired
    private NotificationRepositoryAdapter repository;

    private Notification buildNotification(String email) {
        return Notification.create(
                Recipient.of(email),
                NotificationChannel.EMAIL,
                NotificationTemplate.WELCOME,
                Map.of("name", "João")
        );
    }

    @Test
    @DisplayName("should save and find notification by id")
    void shouldSaveAndFindById() {
        Notification saved = repository.save(buildNotification("joao@nevvesdev.com.br"));

        Optional<Notification> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRecipient().toString()).isEqualTo("joao@nevvesdev.com.br");
        assertThat(found.get().getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    @DisplayName("should find notifications by recipient")
    void shouldFindByRecipient() {
        repository.save(buildNotification("busca@nevvesdev.com.br"));
        repository.save(buildNotification("busca@nevvesdev.com.br"));
        repository.save(buildNotification("outro@nevvesdev.com.br"));

        List<Notification> result = repository.findByRecipient("busca@nevvesdev.com.br");

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).allMatch(n ->
                n.getRecipient().toString().equals("busca@nevvesdev.com.br"));
    }

    @Test
    @DisplayName("should find notifications by status")
    void shouldFindByStatus() {
        repository.save(buildNotification("status@nevvesdev.com.br"));

        List<Notification> pending = repository.findByStatus(NotificationStatus.PENDING);

        assertThat(pending).isNotEmpty();
        assertThat(pending).allMatch(n ->
                n.getStatus() == NotificationStatus.PENDING);
    }

    @Test
    @DisplayName("should persist status transition")
    void shouldPersistStatusTransition() {
        Notification notification = repository.save(buildNotification("transition@nevvesdev.com.br"));
        notification.markAsProcessing();
        notification.recordSuccess();

        repository.save(notification);

        Optional<Notification> found = repository.findById(notification.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("should return empty when notification not found")
    void shouldReturnEmptyWhenNotFound() {
        Optional<Notification> found = repository.findById(
                br.com.nevvesdev.notificationhub.domain.valueobject.NotificationId.generate()
        );

        assertThat(found).isEmpty();
    }
}