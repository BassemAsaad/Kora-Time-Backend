package com.app.koratime.notification.service;

import com.app.koratime.notification.dto.NotificationRequest;

public interface NotificationService {
    void send(NotificationRequest request);
}
