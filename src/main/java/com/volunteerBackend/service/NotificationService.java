package com.volunteerBackend.service;

import java.util.List;

import com.volunteerBackend.model.Notification;
import com.volunteerBackend.request.NotificationRequest;

public interface NotificationService {
    void createNotification(NotificationRequest notification);
    boolean markAsRead (Long notificationId);
    boolean markAllRead (Long userId);
    List<Notification> getAllNotifications(Long userId);
}
