package br.com.nevvesdev.notificationhub.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_NOTIFICATIONS = "notifications.send";
    public static final String TOPIC_DEAD_LETTER   = "notifications.dead-letter";
    public static final String GROUP_ID            = "notification-hub";

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(TOPIC_NOTIFICATIONS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(TOPIC_DEAD_LETTER)
                .partitions(1)
                .replicas(1)
                .build();
    }
}