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
import com.volunteerBackend.payload.DonationSuccessEventPayload;
import com.volunteerBackend.repository.DonationRepository;
import com.volunteerBackend.request.DonateRequest;
import com.volunteerBackend.request.PaymentRequest;
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

    public void CaculatePercent(Long campaignID) {

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

                DonationSuccessEventPayload eventPayload = new DonationSuccessEventPayload();
                eventPayload.setDonateId(donation.getId());
                eventPayload.setCampaignId(donation.getCampaign().getId());
                eventPayload.setUserId(donation.getDonor() != null ? donation.getDonor().getId() : null);
                eventPayload.setAmount(donation.getAmount());
                eventPayload.setDonorEmail(donation.getDonorEmail());
                eventPayload.setDonorName(donation.getDonorName());
                eventPayload.setProjectName(donation.getCampaign().getTitle());
                eventPayload.setTransactionCode(donation.getVnpTransactionNo());

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_DONATION_SUCCESS,
                        "",
                        eventPayload);

                return "success";
            }

        } else {
            return "false";
        }
    }

}
