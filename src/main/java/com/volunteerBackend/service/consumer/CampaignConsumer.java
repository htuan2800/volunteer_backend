package com.volunteerBackend.service.consumer;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.stereotype.Component;
import com.volunteerBackend.config.RabbitMQConfig;
import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.model.Notification.NotificationType;
import com.volunteerBackend.model.User;
import com.volunteerBackend.payload.CampaignStatusPayload;
import com.volunteerBackend.request.NotificationRequest;
import com.volunteerBackend.service.CampaignService;
import com.volunteerBackend.service.DonateService;
import com.volunteerBackend.service.IndexingService;
import com.volunteerBackend.service.NotificationService;

@Component
public class CampaignConsumer {

    
    private IndexingService indexingService;

    
    private CampaignService campaignService;

    
    private NotificationService notificationService;

    
    private DonateService donateService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CAMPAIGN_STATUS_CAMPAIGN)
    public void handleCampaignStatus(CampaignStatusPayload payload) throws Exception {
        Campaign savedCampaign = campaignService.getCampaign(payload.getCampaignId());
        switch (payload.getStatus()) {
            case IN_PROGRESS:
                try {
                    indexingService.updateIndexedCampaign(savedCampaign);
                } catch (Exception e) {
                    System.err.println("Lỗi index vector khi tạo campaign: " + e.getMessage());
                }
                break;
            case ENDED:
                try {
                    indexingService.updateIndexedCampaign(savedCampaign);
                } catch (Exception e) {
                    System.err.println("Lỗi index vector khi update campaign: " + e.getMessage());
                }
                break;
            case PAUSED:
                try {
                    indexingService.updateIndexedCampaign(savedCampaign);
                } catch (Exception e) {
                    System.err.println("Lỗi index vector khi update campaign: " + e.getMessage());
                }
                break;
            case TARGET_REACHED:
                try {
                    indexingService.updateIndexedCampaign(savedCampaign);
                } catch (Exception e) {
                    System.err.println("Lỗi index vector khi update campaign: " + e.getMessage());
                }
                break;
            default:
                break;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CAMPAIGN_STATUS_NOTIFICATION, containerFactory = "myFactory")
    public void handleCampaignNotification(CampaignStatusPayload payload) throws Exception {
        List<User> users = donateService.getUsersByCampaign(payload.getCampaignId());
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle(payload.getTitle());
        notificationRequest.setRelatedId(payload.getCampaignId());
        notificationRequest.setType(NotificationType.CAMPAIGN);
        for (User user : users) {
            switch (payload.getStatus()) {
                case IN_PROGRESS:
                    try {
                        notificationRequest.setUserId(user.getId());
                        notificationRequest.setMessage("Trạng thái của chiến dịch " + payload.getTitle() + " đã hoạt động lại.");
                    } catch (Exception e) {
                        System.err.println("Lỗi index vector khi tạo campaign: " + e.getMessage());
                    }
                    break;
                case ENDED:
                    try {
                        notificationRequest.setUserId(user.getId());
                        notificationRequest.setMessage("Trạng thái của chiến dịch " + payload.getTitle() + " đã kết thúc.");
                    } catch (Exception e) {
                        System.err.println("Lỗi index vector khi update campaign: " + e.getMessage());
                    }
                    break;
                case PAUSED:
                    try {
                        notificationRequest.setUserId(user.getId());
                        notificationRequest.setMessage("Trạng thái của chiến dịch " + payload.getTitle() + " đang tạm dừng.");
                    } catch (Exception e) {
                        System.err.println("Lỗi index vector khi update campaign: " + e.getMessage());
                    }
                    break;
                case TARGET_REACHED:
                    try {
                        notificationRequest.setUserId(user.getId());
                        notificationRequest.setMessage("Trạng thái của chiến dịch " + payload.getTitle() + " đã đạt mục tiêu thành công.");
                    } catch (Exception e) {
                        System.err.println("Lỗi index vector khi update campaign: " + e.getMessage());
                    }
                    break;
                default:
                    break;
            }
            notificationService.createNotification(notificationRequest);
        }
    }

}
