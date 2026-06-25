package com.app.koratime.notification.service;

import com.app.koratime.notification.dto.NotificationRequest;
import com.app.koratime.notification.model.Notification;
import com.app.koratime.notification.repo.NotificationRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationService implements NotificationService {

    private final NotificationRepo notificationRepo;

    @Transactional
    @Override
    public void send(NotificationRequest request) {
        Notification notification = Notification.builder()
                .user(request.recipient())
                .type(request.type())
                .title(request.title())
                .body(request.body())
                .referenceId(request.referenceId())
                .referenceType(request.referenceType())
                .build();

        notificationRepo.save(notification);
        log.debug("In-app notification saved — user: {}, type: {}", request.recipient().getId(), request.type());
    }
}
