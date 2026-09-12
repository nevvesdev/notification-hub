package br.com.nevvesdev.notificationhub.application.usecase;

import br.com.nevvesdev.notificationhub.application.dto.NotificationResponse;
import br.com.nevvesdev.notificationhub.application.port.in.QueryNotificationUseCase;
import br.com.nevvesdev.notificationhub.application.port.out.NotificationRepository;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationStatus;
import br.com.nevvesdev.notificationhub.domain.valueobject.NotificationId;

import java.util.List;
import java.util.Optional;

public class QueryNotificationUseCaseImpl implements QueryNotificationUseCase {

    private final NotificationRepository repository;

    public QueryNotificationUseCaseImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<NotificationResponse> findById(String id) {
        return repository.findById(NotificationId.of(id))
                .map(NotificationResponse::from);
    }

    @Override
    public List<NotificationResponse> findByRecipient(String recipient) {
        return repository.findByRecipient(recipient)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    public List<NotificationResponse> findPending() {
        return repository.findByStatus(NotificationStatus.PENDING)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }
}