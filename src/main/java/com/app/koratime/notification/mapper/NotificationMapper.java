package com.app.koratime.notification.mapper;

import com.app.koratime.notification.dto.NotificationResponse;
import com.app.koratime.notification.model.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
