package com.app.koratime.notification.dto;

import com.app.koratime.notification.model.NotificationType;
import com.app.koratime.user.model.User;
import lombok.Builder;

import java.util.UUID;

@Builder
public record NotificationRequest(
        User recipient,
        NotificationType type,
        String title,
        String body,
        UUID referenceId,
        String referenceType
) {

    public static NotificationRequest of(User recipient, NotificationType type, String title, String body) {
        return NotificationRequest.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .body(body)
                .build();
    }

    public static NotificationRequest of(
            User recipient,
            NotificationType type,
            String title,
            String body,
            UUID referenceId,
            String referenceType
    ) {
        return NotificationRequest.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .body(body)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
    }
}
