package com.volunteerBackend.service.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.volunteerBackend.config.RabbitMQConfig;
import com.volunteerBackend.model.Notification.NotificationType;
import com.volunteerBackend.payload.DonationSuccessEventPayload;
import com.volunteerBackend.request.NotificationRequest;
import com.volunteerBackend.service.CampaignService;
import com.volunteerBackend.service.DashboardStatisticsService;
import com.volunteerBackend.service.EmailService;
import com.volunteerBackend.service.NotificationService;
import com.volunteerBackend.type.CampaignStatus;

@Component
public class DonationConsumer {
    @Autowired
    private DashboardStatisticsService dashboardStatisticsService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues =  RabbitMQConfig.QUEUE_DONATION_SUCCESS_NOTIFICATION)
    public void handleDonationNotification(DonationSuccessEventPayload payload) {
        if(payload.getUserId() == null) {
            return;
        } else{
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setUserId(payload.getUserId());
            notificationRequest.setTitle(payload.getProjectName());
            notificationRequest.setMessage("Bạn đã thanh toán thành công số tiền cho dự án " + payload.getProjectName());
            notificationRequest.setRelatedId(payload.getCampaignId());
            notificationRequest.setType(NotificationType.DONATION);
            notificationService.createNotification(notificationRequest);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DONATION_SUCCESS_DASHBOARD)
    public void handleDashboardUpdate(DonationSuccessEventPayload payload) {
        dashboardStatisticsService.updateTotalDonationsAmount(payload.getAmount());
        dashboardStatisticsService.updateTotalSupportCount();
        dashboardStatisticsService.getCaculatePercentage(payload.getCampaignId());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DONATION_SUCCESS_CAMPAIGN)
    public void handleCampaignStatus(DonationSuccessEventPayload payload) {
        Boolean isTargetAmountReached = campaignService.isTargetAmountReached(payload.getCampaignId());
        if (isTargetAmountReached) {
            campaignService.changeCampaignStatus(payload.getCampaignId(), CampaignStatus.TARGET_REACHED);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DONATION_SUCCESS_EMAIL)
    public void handleDonationEmail(DonationSuccessEventPayload payload) {
        try {
            emailService.sendDonationThankYouEmail(
                    payload.getDonorEmail(),
                    payload.getDonorName(),
                    payload.getAmount(),
                    payload.getProjectName(),
                    payload.getTransactionCode());
        } catch (Exception e) {
            System.err.println(
                    "ERROR: Consumer failed to process email for " + payload.getDonorEmail() + ". Reason: "
                            + e.getMessage());
        }
    }
}
