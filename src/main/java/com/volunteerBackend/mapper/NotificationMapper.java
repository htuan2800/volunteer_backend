package com.volunteerBackend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.NotificationDTO;
import com.volunteerBackend.model.Notification;

@Component
public class NotificationMapper {
    
    public NotificationDTO toNotificationDTO(Notification notification) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setNotificationID(notification.getId());
        notificationDTO.setTitle(notification.getTitle());
        notificationDTO.setMessage(notification.getMessage());
        notificationDTO.setType(notification.getType());
        notificationDTO.setUserId(notification.getUser().getId());
        notificationDTO.setRelatedId(notification.getRelatedId());
        notificationDTO.setIsRead(notification.getIsRead());
        notificationDTO.setCreatedAt(notification.getCreatedAt());
        notificationDTO.setReadAt(notification.getReadAt());
        return notificationDTO;
    }

    public List<NotificationDTO> toNotificationDTOList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toNotificationDTO)
                .collect(Collectors.toList());
    }
}
