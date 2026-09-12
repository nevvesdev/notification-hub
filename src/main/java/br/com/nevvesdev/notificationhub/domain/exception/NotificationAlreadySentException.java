package br.com.nevvesdev.notificationhub.domain.exception;

import br.com.nevvesdev.notificationhub.shared.exception.DomainException;

public class NotificationAlreadySentException extends DomainException {

    public NotificationAlreadySentException(String notificationId) {
        super("Notification already sent and cannot be reprocessed: " + notificationId);
    }
}