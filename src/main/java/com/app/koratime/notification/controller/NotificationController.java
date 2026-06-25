package com.app.koratime.notification.controller;

import com.app.koratime.common.response.ApiResponse;
import com.app.koratime.notification.dto.NotificationResponse;
import com.app.koratime.notification.service.NotificationInboxService;
import com.app.koratime.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationInboxService inboxService;

    @GetMapping
    @Operation(summary = "Get my notification inbox, newest first")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(inboxService.list(pageable)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications (for the badge icon)")
    public ResponseEntity<ApiResponse<Long>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.success(inboxService.unreadCount()));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String id) {
        inboxService.markAsRead(UUID.fromString(id));
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark every notification as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        inboxService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }


}
