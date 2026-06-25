package com.app.koratime.notification.service;

import com.app.koratime.common.exception.ResourceNotFoundException;
import com.app.koratime.common.security.SecurityUtils;
import com.app.koratime.notification.dto.NotificationResponse;
import com.app.koratime.notification.mapper.NotificationMapper;
import com.app.koratime.notification.model.Notification;
import com.app.koratime.notification.repo.NotificationRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationInboxServiceImpl implements NotificationInboxService {

    private final NotificationRepo notificationRepo;
    private final NotificationMapper notificationMapper;

    @Override
    public Page<NotificationResponse> list(Pageable pageable) {
        return notificationRepo
                .findByUserIdOrderByCreatedAtDesc(SecurityUtils.getCurrentUserId(), pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public long unreadCount() {
        return notificationRepo.countByUserIdAndReadFalse(SecurityUtils.getCurrentUserId());
    }

    @Transactional
    @Override
    public void markAsRead(UUID notificationId) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Notification notification = notificationRepo
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        notification.setRead(true);
        notificationRepo.save(notification);

    }

    @Override
    public void markAllAsRead() {
        notificationRepo.markAllAsRead(SecurityUtils.getCurrentUserId());
    }
}
