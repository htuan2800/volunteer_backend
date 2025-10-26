package com.volunteerBackend.service;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.volunteerBackend.DTO.NotificationDTO;
import com.volunteerBackend.config.RabbitMQConfig;
import com.volunteerBackend.mapper.NotificationMapper;
import com.volunteerBackend.model.Notification;
import com.volunteerBackend.model.User;
import com.volunteerBackend.repository.NotificationRepository;
import com.volunteerBackend.repository.UserRepository;
import com.volunteerBackend.request.NotificationRequest;

@Service
public class NotificationServiceImp implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationMapper notificationMapper;
    
    @Override
    public void createNotification(NotificationRequest notification) {
        User user = userRepository.findById(notification.getUserId()).orElse(null);
        Notification newNotification = new Notification();
        newNotification.setTitle(notification.getTitle());
        newNotification.setMessage(notification.getMessage());
        newNotification.setType(notification.getType());
        newNotification.setRelatedId(notification.getRelatedId());
        newNotification.setIsRead(false);
        newNotification.setUser(user);
        notificationRepository.save(newNotification);
        NotificationDTO newNotificationDTO = notificationMapper.toNotificationDTO(newNotification);
        switch (notification.getType()) {
            case SYSTEM:
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NOTIFICATION_NAME,
                    RabbitMQConfig.ROUTING_KEY_NOTIFICATION_SYSTEM, 
                    newNotificationDTO
                );
                break;
            case DONATION:
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NOTIFICATION_NAME,
                    RabbitMQConfig.ROUTING_KEY_NOTIFICATION_DONATION, 
                    newNotificationDTO
                );
                break;
            case CAMPAIGN:
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NOTIFICATION_NAME,
                    RabbitMQConfig.ROUTING_KEY_NOTIFICATION_CAMPAIGN, 
                    newNotificationDTO
                );
                break;
        }
    }

    @Override
    public boolean markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return true;
    }

    @Override
    public boolean markAllRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
        return true;
    }

    @Override
    public List<Notification> getAllNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
}
