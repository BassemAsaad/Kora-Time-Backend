package com.app.koratime.notification.service;

import com.app.koratime.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final List<NotificationService> channels;

    public void dispatch(NotificationRequest request) {
        for (NotificationService channel : channels) {
            try {
                channel.send(request);
            } catch (Exception e) {
                log.error("Notification channel {} failed for user {}: {}",
                        channel.getClass().getSimpleName(),
                        request.recipient().getId(),
                        e.getMessage());
            }
        }
    }
}
