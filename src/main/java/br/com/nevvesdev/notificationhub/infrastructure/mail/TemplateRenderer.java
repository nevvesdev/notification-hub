package br.com.nevvesdev.notificationhub.infrastructure.mail;

import br.com.nevvesdev.notificationhub.domain.enums.NotificationTemplate;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
public class TemplateRenderer {

    private final TemplateEngine templateEngine;

    public TemplateRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String render(NotificationTemplate template, Map<String, Object> payload) {
        Context context = new Context();
        context.setVariable("payload", payload);

        return templateEngine.process(template.getTemplateName(), context);
    }
}