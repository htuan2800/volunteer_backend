package com.volunteerBackend.service.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.NotificationDTO;
import com.volunteerBackend.config.RabbitMQConfig;
@Component
public class NotificationConsumer {
    
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION_DONATION, containerFactory = "myFactory")
    public void handleDonationNotification(NotificationDTO notification) {
        System.out.println("Received notification: " + notification);
        messagingTemplate.convertAndSend("/topic/notifications/"+notification.getUserId(), notification);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION_CAMPAIGN, containerFactory = "myFactory")
    public void handleCampaignNotification(NotificationDTO notification) {
        System.out.println("Received notification: " + notification);
        messagingTemplate.convertAndSend("/topic/notifications/"+notification.getUserId(), notification);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION_SYSTEM, containerFactory = "myFactory")
    public void handleSystemNotification(NotificationDTO notification) {
        System.out.println("Received notification: " + notification);
        messagingTemplate.convertAndSend("/topic/notifications/"+notification.getUserId(), notification);
    }
}
