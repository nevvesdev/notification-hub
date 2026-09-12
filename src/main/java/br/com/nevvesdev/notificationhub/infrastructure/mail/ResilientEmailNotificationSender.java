package br.com.nevvesdev.notificationhub.infrastructure.mail;

import br.com.nevvesdev.notificationhub.application.port.out.NotificationSender;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.model.Notification;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class ResilientEmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(ResilientEmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final TemplateRenderer templateRenderer;

    @Value("${notification.mail.from}")
    private String from;

    @Value("${notification.mail.from-name}")
    private String fromName;

    public ResilientEmailNotificationSender(
            JavaMailSender mailSender,
            TemplateRenderer templateRenderer
    ) {
        this.mailSender = mailSender;
        this.templateRenderer = templateRenderer;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    @Retry(name = "emailSender")
    @CircuitBreaker(name = "emailSender", fallbackMethod = "fallback")
    public void send(Notification notification) {
        String htmlBody = templateRenderer.render(
                notification.getTemplate(),
                notification.getPayload()
        );

        String subject = resolveSubject(notification);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from, fromName);
            helper.setTo(notification.getRecipient().toString());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

            log.info("Email sent id={} to={} subject={}",
                    notification.getId(),
                    notification.getRecipient(),
                    subject);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send email to " + notification.getRecipient(), e);
        }
    }

    // Chamado pelo Circuit Breaker quando o circuito está aberto
    public void fallback(Notification notification, Throwable cause) {
        log.error("Circuit breaker OPEN — email not sent id={} to={} reason={}",
                notification.getId(),
                notification.getRecipient(),
                cause.getMessage());

        throw new RuntimeException(
                "Email service unavailable (circuit open) for recipient: "
                        + notification.getRecipient(), cause
        );
    }

    private String resolveSubject(Notification notification) {
        Object customSubject = notification.getPayload().get("subject");
        if (customSubject instanceof String s && !s.isBlank()) {
            return s;
        }
        return notification.getTemplate().getDefaultSubject();
    }
}