package br.com.nevvesdev.notificationhub.infrastructure.health;

import br.com.nevvesdev.notificationhub.application.port.out.NotificationRepository;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationStatus;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("notifications")
public class NotificationHealthIndicator implements HealthIndicator {

    private final NotificationRepository repository;

    public NotificationHealthIndicator(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Health health() {
        try {
            int pending = repository.findByStatus(NotificationStatus.PENDING).size();
            int failed  = repository.findByStatus(NotificationStatus.FAILED).size();
            int dead    = repository.findByStatus(NotificationStatus.DEAD_LETTERED).size();

            Health.Builder builder = (dead > 0 || failed > 10)
                    ? Health.down()
                    : Health.up();

            return builder
                    .withDetail("pending", pending)
                    .withDetail("failed", failed)
                    .withDetail("dead-lettered", dead)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}