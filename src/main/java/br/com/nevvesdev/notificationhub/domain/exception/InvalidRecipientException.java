package br.com.nevvesdev.notificationhub.domain.exception;

import br.com.nevvesdev.notificationhub.shared.exception.DomainException;

public class InvalidRecipientException extends DomainException {

    public InvalidRecipientException(String message) {
        super(message);
    }
}