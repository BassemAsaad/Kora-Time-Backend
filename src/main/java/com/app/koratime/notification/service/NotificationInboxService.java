package com.app.koratime.notification.service;

import com.app.koratime.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationInboxService {

    Page<NotificationResponse> list(Pageable pageable);

    long unreadCount();

    void markAsRead(UUID notificationId);

    void markAllAsRead();
}
