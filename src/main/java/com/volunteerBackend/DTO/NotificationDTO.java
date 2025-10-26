package com.volunteerBackend.DTO;

import java.time.LocalDateTime;

import com.volunteerBackend.model.Notification.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long notificationID;
    private String title;
    private String message;
    private Integer userId;
    private NotificationType type;
    private Long relatedId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
