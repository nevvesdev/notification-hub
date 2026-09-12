package br.com.nevvesdev.notificationhub.domain.valueobject;

import br.com.nevvesdev.notificationhub.domain.exception.InvalidRecipientException;

import java.util.regex.Pattern;

public record Recipient(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$");

    public Recipient {
        if (value == null || value.isBlank()) {
            throw new InvalidRecipientException("Recipient cannot be blank");
        }
        if (!EMAIL_PATTERN.matcher(value.strip()).matches()) {
            throw new InvalidRecipientException("Invalid email address: " + value);
        }
    }

    public static Recipient of(String value) {
        return new Recipient(value.strip().toLowerCase());
    }

    @Override
    public String toString() {
        return value;
    }
}