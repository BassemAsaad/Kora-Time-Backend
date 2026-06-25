package com.app.koratime.notification.dto;

import com.app.koratime.notification.model.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationResponse (
        UUID id,
        NotificationType type,
        String title,
        String body,
        UUID referenceId,
        String referenceType,
        boolean read,
        LocalDateTime createdAt
) {
}
