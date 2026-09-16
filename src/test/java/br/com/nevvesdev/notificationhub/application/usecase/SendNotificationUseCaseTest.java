package br.com.nevvesdev.notificationhub.application.usecase;

import br.com.nevvesdev.notificationhub.application.dto.NotificationResponse;
import br.com.nevvesdev.notificationhub.application.dto.SendNotificationCommand;
import br.com.nevvesdev.notificationhub.application.port.out.DeadLetterPublisher;
import br.com.nevvesdev.notificationhub.application.port.out.NotificationRepository;
import br.com.nevvesdev.notificationhub.application.port.out.NotificationSender;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationStatus;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationTemplate;
import br.com.nevvesdev.notificationhub.domain.model.Notification;
import br.com.nevvesdev.notificationhub.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendNotificationUseCase")
class SendNotificationUseCaseTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationSender sender;

    @Mock
    private DeadLetterPublisher deadLetterPublisher;

    private SendNotificationUseCaseImpl useCase;

    private SendNotificationCommand command;

    @BeforeEach
    void setUp() {
        when(sender.supports(NotificationChannel.EMAIL)).thenReturn(true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase = new SendNotificationUseCaseImpl(repository, List.of(sender), deadLetterPublisher);

        command = new SendNotificationCommand(
                "joao@nevvesdev.com.br",
                NotificationChannel.EMAIL,
                NotificationTemplate.WELCOME,
                Map.of("name", "João")
        );
    }

    @Test
    @DisplayName("should send notification successfully")
    void shouldSendSuccessfully() {
        NotificationResponse response = useCase.execute(command);

        assertThat(response.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.recipient()).isEqualTo("joao@nevvesdev.com.br");
        assertThat(response.attemptCount()).isEqualTo(1);

        verify(sender).send(any(Notification.class));
        verify(deadLetterPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should save notification before and after sending")
    void shouldSaveBeforeAndAfterSending() {
        useCase.execute(command);

        // PENDING + PROCESSING + SENT = 3 saves
        verify(repository, times(3)).save(any(Notification.class));
    }

    @Test
    @DisplayName("should record failure and mark as FAILED on first error")
    void shouldRecordFailureOnFirstError() {
        doThrow(new RuntimeException("SMTP timeout")).when(sender).send(any());

        NotificationResponse response = useCase.execute(command);

        assertThat(response.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(response.attemptCount()).isEqualTo(1);
        verify(deadLetterPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should throw when no sender supports the channel")
    void shouldThrowWhenNoSenderSupportsChannel() {
        when(sender.supports(NotificationChannel.EMAIL)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("No sender available for channel");
    }

    @Test
    @DisplayName("should capture correct recipient on notification")
    void shouldCaptureCorrectRecipient() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        useCase.execute(command);

        verify(sender).send(captor.capture());
        assertThat(captor.getValue().getRecipient().toString())
                .isEqualTo("joao@nevvesdev.com.br");
    }
}