package br.com.nevvesdev.notificationhub.infrastructure.config;

import br.com.nevvesdev.notificationhub.application.port.in.QueryNotificationUseCase;
import br.com.nevvesdev.notificationhub.application.port.in.SendNotificationUseCase;
import br.com.nevvesdev.notificationhub.application.port.out.DeadLetterPublisher;
import br.com.nevvesdev.notificationhub.application.port.out.NotificationRepository;
import br.com.nevvesdev.notificationhub.application.port.out.NotificationSender;
import br.com.nevvesdev.notificationhub.application.usecase.QueryNotificationUseCaseImpl;
import br.com.nevvesdev.notificationhub.application.usecase.SendNotificationUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class UseCaseConfig {

    @Bean
    public SendNotificationUseCase sendNotificationUseCase(
            NotificationRepository repository,
            List<NotificationSender> senders,
            DeadLetterPublisher deadLetterPublisher
    ) {
        return new SendNotificationUseCaseImpl(repository, senders, deadLetterPublisher);
    }

    @Bean
    public QueryNotificationUseCase queryNotificationUseCase(
            NotificationRepository repository
    ) {
        return new QueryNotificationUseCaseImpl(repository);
    }
}