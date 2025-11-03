package com.volunteerBackend.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.DTO.NotificationDTO;
import com.volunteerBackend.mapper.NotificationMapper;
import com.volunteerBackend.model.Notification;
import com.volunteerBackend.service.NotificationService;


@RestController
@RequestMapping("/api")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationMapper notificationMapper;

    @GetMapping("/notifications/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getAllNotifications(userId);
        List<NotificationDTO> notificationsDTO = notificationMapper.toNotificationDTOList(notifications);
        return new ResponseEntity<>(notificationsDTO, HttpStatus.OK);
    }

    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long notificationId) {
        boolean result = notificationService.markAsRead(notificationId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/notifications/user/{userId}/read-all")
    public ResponseEntity<?> markAllNotificationsAsRead(@PathVariable Long userId) {
        boolean result = notificationService.markAllRead(userId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
