package br.com.nevvesdev.notificationhub.infrastructure.mail;

import br.com.nevvesdev.notificationhub.application.port.out.NotificationSender;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.model.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final TemplateRenderer templateRenderer;

    @Value("${notification.mail.from}")
    private String from;

    @Value("${notification.mail.from-name}")
    private String fromName;

    public EmailNotificationSender(
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

    private String resolveSubject(Notification notification) {
        Object customSubject = notification.getPayload().get("subject");
        if (customSubject instanceof String s && !s.isBlank()) {
            return s;
        }
        return notification.getTemplate().getDefaultSubject();
    }
}