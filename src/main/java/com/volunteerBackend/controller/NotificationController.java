package com.volunteerBackend.controller;
import java.util.List;


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

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    private final NotificationMapper notificationMapper;

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
