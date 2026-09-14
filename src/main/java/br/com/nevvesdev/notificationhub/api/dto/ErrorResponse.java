package br.com.nevvesdev.notificationhub.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        List<FieldError> fields,
        Instant timestamp
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, null, Instant.now());
    }

    public static ErrorResponse of(int status, String error, String message, List<FieldError> fields) {
        return new ErrorResponse(status, error, message, fields, Instant.now());
    }
}