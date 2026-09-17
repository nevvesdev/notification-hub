package br.com.nevvesdev.notificationhub.infrastructure.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component("kafka")
public class KafkaHealthIndicator implements HealthIndicator {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Override
    public Health health() {
        try (AdminClient client = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000",
                AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "3000"
        ))) {
            client.listTopics().names().get(3, TimeUnit.SECONDS);

            return Health.up()
                    .withDetail("bootstrap-servers", bootstrapServers)
                    .withDetail("status", "reachable")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("bootstrap-servers", bootstrapServers)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}