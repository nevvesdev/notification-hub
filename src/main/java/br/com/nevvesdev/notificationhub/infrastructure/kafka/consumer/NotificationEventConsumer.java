package br.com.nevvesdev.notificationhub.infrastructure.kafka.consumer;

import br.com.nevvesdev.notificationhub.application.dto.SendNotificationCommand;
import br.com.nevvesdev.notificationhub.application.port.in.SendNotificationUseCase;
import br.com.nevvesdev.notificationhub.infrastructure.config.KafkaConfig;
import br.com.nevvesdev.notificationhub.infrastructure.kafka.dto.NotificationEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final SendNotificationUseCase sendNotificationUseCase;

    public NotificationEventConsumer(SendNotificationUseCase sendNotificationUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_NOTIFICATIONS,
            groupId = KafkaConfig.GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload NotificationEventDTO event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Event received topic={} partition={} offset={} recipient={}",
                topic, partition, offset, event.recipient());

        try {
            SendNotificationCommand command = new SendNotificationCommand(
                    event.recipient(),
                    event.channel(),
                    event.template(),
                    event.payload() != null ? event.payload() : java.util.Map.of()
            );

            sendNotificationUseCase.execute(command);

        } catch (Exception e) {
            log.error("Failed to process event offset={} recipient={} reason={}",
                    offset, event.recipient(), e.getMessage(), e);
            // Lança para o Kafka retentar ou mover para DLQ via error handler
            throw e;
        }
    }
}