package br.com.nevvesdev.notificationhub.api.controller;

import br.com.nevvesdev.notificationhub.api.dto.ApiResponse;
import br.com.nevvesdev.notificationhub.application.dto.NotificationResponse;
import br.com.nevvesdev.notificationhub.application.dto.SendNotificationCommand;
import br.com.nevvesdev.notificationhub.application.port.in.QueryNotificationUseCase;
import br.com.nevvesdev.notificationhub.application.port.in.SendNotificationUseCase;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationChannel;
import br.com.nevvesdev.notificationhub.domain.enums.NotificationTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final QueryNotificationUseCase queryNotificationUseCase;

    public NotificationController(
            SendNotificationUseCase sendNotificationUseCase,
            QueryNotificationUseCase queryNotificationUseCase
    ) {
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.queryNotificationUseCase = queryNotificationUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> send(
            @Valid @RequestBody SendRequest request
    ) {
        SendNotificationCommand command = new SendNotificationCommand(
                request.recipient(),
                request.channel(),
                request.template(),
                request.payload() != null ? request.payload() : Map.of()
        );

        NotificationResponse response = sendNotificationUseCase.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Notification queued successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> findById(
            @PathVariable String id
    ) {
        return queryNotificationUseCase.findById(id)
                .map(notification -> ResponseEntity.ok(ApiResponse.ok(notification)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> findByRecipient(
            @RequestParam @Email String recipient
    ) {
        List<NotificationResponse> notifications =
                queryNotificationUseCase.findByRecipient(recipient);

        return ResponseEntity.ok(ApiResponse.ok(notifications));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> findPending() {
        List<NotificationResponse> pending = queryNotificationUseCase.findPending();
        return ResponseEntity.ok(ApiResponse.ok(pending));
    }

    // Request DTO — vive no controller, não vaza pro domínio
    public record SendRequest(
            @NotBlank(message = "Recipient is required")
            @Email(message = "Recipient must be a valid email")
            String recipient,

            @NotNull(message = "Channel is required")
            NotificationChannel channel,

            @NotNull(message = "Template is required")
            NotificationTemplate template,

            Map<String, Object> payload
    ) {}
}