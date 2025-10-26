package com.volunteerBackend.service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.volunteerBackend.config.RabbitMQConfig;
import com.volunteerBackend.model.Donation;
import com.volunteerBackend.model.User;
import com.volunteerBackend.model.Notification.NotificationType;
import com.volunteerBackend.payload.DonationSuccessEmailPayload;
import com.volunteerBackend.repository.DonationRepository;
import com.volunteerBackend.request.DonateRequest;
import com.volunteerBackend.request.NotificationRequest;
import com.volunteerBackend.request.PaymentRequest;
import com.volunteerBackend.type.CampaignStatus;
import com.volunteerBackend.type.PaymentMethod;
import com.volunteerBackend.type.PaymentStatus;
import com.volunteerBackend.util.VnpayUtil;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class DonateServiceImp implements DonateService {
    @Autowired
    private DonationRepository donationRepository;
    @Autowired
    private VnPayService vnPayService;
    @Autowired
    private CampaignService campaignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DashboardStatisticsService dashboardStatisticsService;

    @Autowired
    private NotificationService notificationService;


    @Override
    public List<Donation> getAllDonates() {
        return donationRepository.findAll();
    }

    @Override
    public List<Donation> getAllDonatesByDonorName(String donorName) {
        return donationRepository.findByDonorNameContaining(donorName);
    }

    @Override
    public List<Donation> getAllDonatesByCampaign(Long campaignID) {
        return donationRepository.findByCampaignIdAndPaymentStatus(campaignID, PaymentStatus.COMPLETED);
    }

    @Override
    public Donation getDonationById(Long donateID) {
        return donationRepository.findById(donateID).orElse(null);
    }

    public void CaculatePercent (Long campaignID) {
        
    }

    @Override
    public String createDonate(HttpServletRequest request, DonateRequest donateRequest, User user)
            throws UnsupportedEncodingException {
        Donation donation = new Donation();
        donation.setCampaign(campaignService.getCampaign(donateRequest.getCampaign_id()));
        donation.setAmount(donateRequest.getDonor_amount());
        donation.setDonorEmail(donateRequest.getDonor_email());
        donation.setDonorName(donateRequest.getDonor_name());
        donation.setDonorPhone(donateRequest.getDonor_phone());
        donation.setMessage(donateRequest.getMessage());
        donation.setIsAnonymous(donateRequest.isAnonymous());
        donation.setVnpTxnRef(VnpayUtil.getRandomNumber(8));
        donation.setPaymentMethod(PaymentMethod.VNPAY);
        if (user == null) {
            Donation newDonation = donationRepository.save(donation);
            return vnPayService.createPaymentUrl(request, newDonation.getId(), newDonation.getAmount(),
                    newDonation.getVnpTxnRef());
        } else {
            donation.setDonor(user);
            Donation newDonation = donationRepository.save(donation);
            return vnPayService.createPaymentUrl(request, newDonation.getId(), newDonation.getAmount(),
                    newDonation.getVnpTxnRef());
        }
    }

    @Override
    public String updateDonate(String donateId, PaymentRequest paymentRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime localDateTime = LocalDateTime.parse(paymentRequest.getVnp_PayDate(), formatter);
        Donation donation = donationRepository.findByVnpTxnRef(donateId);
        if (donation != null) {
            donation.setVnpTransactionNo(paymentRequest.getVnp_TransactionNo());
            donation.setVnpBankTranNo(paymentRequest.getVnp_BankTranNo());
            donation.setVnpBankCode(paymentRequest.getVnp_BankCode());
            donation.setVnpResponseCode(paymentRequest.getVnp_ResponseCode());
            donation.setPaymentDate(localDateTime);
            if (!paymentRequest.getVnp_ResponseCode().equals("00")) {
                donation.setPaymentStatus(PaymentStatus.FAILED);
                donationRepository.save(donation);
                return "failed";
            } else {
                donation.setPaymentStatus(PaymentStatus.COMPLETED);
                donationRepository.save(donation);

                Boolean isTargetAmountReached = campaignService.isTargetAmountReached(donation.getCampaign().getId());
                if (isTargetAmountReached) {
                    campaignService.changeCampaignStatus(donation.getCampaign().getId(), CampaignStatus.TARGET_REACHED);
                }

                dashboardStatisticsService.updateTotalDonationsAmount(donation.getAmount());
                dashboardStatisticsService.updateTotalSupportCount();

                // Chuẩn bị payload để gửi email
                DonationSuccessEmailPayload payload = new DonationSuccessEmailPayload();
                payload.setTo(donation.getDonorEmail());
                payload.setFullName(donation.getDonorName());
                payload.setAmount(donation.getAmount());
                payload.setProjectName(donation.getCampaign().getTitle());
                payload.setTransactionCode(donation.getVnpTransactionNo());

                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setUserId(donation.getDonor().getId());
                notificationRequest.setTitle(donation.getCampaign().getTitle());
                notificationRequest.setMessage("Bạn đã thanh toán thành công số tiền cho dự án " + donation.getCampaign().getTitle());
                notificationRequest.setRelatedId(donation.getCampaign().getId());
                notificationRequest.setType(NotificationType.DONATION);

                //Khi người dùng hiện tại thanh toán thành công
                notificationService.createNotification(notificationRequest);
                // Đẩy vào RabbitMQ để một tiến trình khác xử lý việc gửi email
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_USER_NAME,
                        RabbitMQConfig.ROUTING_KEY_SENT_EMAIL_DONATION_SUCCESS,
                        payload);
                
                dashboardStatisticsService.getCaculatePercentage(donation.getCampaign().getId());
                return "success";
            }

        } else {
            return "false";
        }
    }

}
