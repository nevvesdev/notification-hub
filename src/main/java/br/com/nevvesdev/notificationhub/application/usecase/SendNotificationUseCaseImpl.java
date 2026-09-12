package br.com.nevvesdev.notificationhub.application.usecase;

import br.com.nevvesdev.notificationhub.application.dto.NotificationResponse;
import br.com.nevvesdev.notificationhub.application.dto.SendNotificationCommand;
import br.com.nevvesdev.notificationhub.application.port.in.SendNotificationUseCase;
import br.com.nevvesdev.notificationhub.application.port.out.DeadLetterPublisher;
import br.com.nevvesdev.notificationhub.application.port.out.NotificationRepository;
import br.com.nevvesdev.notificationhub.application.port.out.NotificationSender;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.model.Notification;
import br.com.nevvesdev.notificationhub.domain.valueobject.Recipient;
import br.com.nevvesdev.notificationhub.shared.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SendNotificationUseCaseImpl implements SendNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendNotificationUseCaseImpl.class);

    private final NotificationRepository repository;
    private final List<NotificationSender> senders;
    private final DeadLetterPublisher deadLetterPublisher;

    public SendNotificationUseCaseImpl(
            NotificationRepository repository,
            List<NotificationSender> senders,
            DeadLetterPublisher deadLetterPublisher
    ) {
        this.repository = repository;
        this.senders = senders;
        this.deadLetterPublisher = deadLetterPublisher;
    }

    @Override
    public NotificationResponse execute(SendNotificationCommand command) {
        log.info("Processing notification for recipient={} channel={} template={}",
                command.recipient(), command.channel(), command.template());

        Notification notification = Notification.create(
                Recipient.of(command.recipient()),
                command.channel(),
                command.template(),
                command.payload()
        );

        repository.save(notification);

        NotificationSender sender = resolveSender(command.channel());
        notification.markAsProcessing();
        repository.save(notification);

        try {
            sender.send(notification);
            notification.recordSuccess();
            log.info("Notification sent successfully id={}", notification.getId());
        } catch (Exception e) {
            log.warn("Failed to send notification id={} attempt={} reason={}",
                    notification.getId(), notification.getAttemptCount() + 1, e.getMessage());

            notification.recordFailure(e.getMessage());

            if (notification.getStatus().isTerminal()) {
                log.error("Notification exhausted all attempts, sending to DLQ id={}", notification.getId());
                deadLetterPublisher.publish(notification);
            }
        }

        return NotificationResponse.from(repository.save(notification));
    }

    private NotificationSender resolveSender(NotificationChannel channel) {
        return senders.stream()
                .filter(s -> s.supports(channel))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "No sender available for channel: " + channel
                ));
    }
}