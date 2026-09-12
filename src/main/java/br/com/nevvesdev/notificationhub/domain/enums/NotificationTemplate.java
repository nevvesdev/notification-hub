package br.com.nevvesdev.notificationhub.domain.enums;

public enum NotificationTemplate {
    WELCOME("welcome", "Bem-vindo!"),
    PASSWORD_RESET("password-reset", "Redefinição de senha"),
    ORDER_CONFIRMED("order-confirmed", "Pedido confirmado"),
    GENERIC("generic", "Notificação");

    private final String templateName;
    private final String defaultSubject;

    NotificationTemplate(String templateName, String defaultSubject) {
        this.templateName = templateName;
        this.defaultSubject = defaultSubject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDefaultSubject() {
        return defaultSubject;
    }
}