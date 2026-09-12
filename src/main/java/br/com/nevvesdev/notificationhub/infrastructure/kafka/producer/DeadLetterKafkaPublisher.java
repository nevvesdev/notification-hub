package br.com.nevvesdev.notificationhub.infrastructure.kafka.producer;

import br.com.nevvesdev.notificationhub.application.port.out.DeadLetterPublisher;
import br.com.nevvesdev.notificationhub.domain.model.Notification;
import br.com.nevvesdev.notificationhub.infrastructure.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterKafkaPublisher implements DeadLetterPublisher {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterKafkaPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeadLetterKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(Notification notification) {
        log.warn("Publishing to DLQ id={} recipient={} attempts={}",
                notification.getId(),
                notification.getRecipient(),
                notification.getAttemptCount());

        kafkaTemplate.send(
                KafkaConfig.TOPIC_DEAD_LETTER,
                notification.getId().toString(),
                buildPayload(notification)
        );
    }

    private DeadLetterPayload buildPayload(Notification notification) {
        return new DeadLetterPayload(
                notification.getId().toString(),
                notification.getRecipient().toString(),
                notification.getChannel().name(),
                notification.getTemplate().name(),
                notification.getAttemptCount(),
                notification.getUpdatedAt().toString()
        );
    }

    public record DeadLetterPayload(
            String notificationId,
            String recipient,
            String channel,
            String template,
            int attempts,
            String failedAt
    ) {}
}