package com.volunteerBackend.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteerBackend.config.MomoConfig;
import com.volunteerBackend.config.RabbitMQConfig;
import com.volunteerBackend.config.VnpayConfig;
import com.volunteerBackend.model.Donation;
import com.volunteerBackend.model.User;
import com.volunteerBackend.payload.DonationSuccessEventPayload;
import com.volunteerBackend.repository.DonationRepository;
import com.volunteerBackend.request.DonateRequest;
import com.volunteerBackend.request.MoMoIPN;
import com.volunteerBackend.response.VnPayIpnResponse;
import com.volunteerBackend.type.PaymentMethod;
import com.volunteerBackend.type.PaymentStatus;
import com.volunteerBackend.util.MomoUtil;
import com.volunteerBackend.util.VnpayUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DonateServiceImp implements DonateService {
    
    private final DonationRepository donationRepository;
    
    private final VnPayService vnPayService;
    
    private final MomoService momoService;
    
    private final CampaignService campaignService;

    
    private final RabbitTemplate rabbitTemplate;

    
    private final VnpayConfig vnpayConfig;

    
    private final MomoConfig momoConfig;

    @Override
    public List<Donation> getAllDonates() {
        return donationRepository.findAll();
    }

    @Override
    public List<Donation> getAllDonatesByDonorName(String donorName) {
        return donationRepository.findByDonorNameContainingIgnoreCase(donorName);
    }

    @Override
    public List<Donation> getAllDonatesByCampaign(Long campaignID) {
        return donationRepository.findByCampaignIdAndPaymentStatus(campaignID, PaymentStatus.COMPLETED);
    }

    @Override
    public List<User> getUsersByCampaign(Long campaignID) {
        return donationRepository.findDistincUsersByCampaignId(campaignID);
    }

    @Override
    public Donation getDonationById(Long donateID) {
        return donationRepository.findById(donateID).orElse(null);
    }

    public void CaculatePercent(Long campaignID) {

    }

    @Override
    public String createDonate(HttpServletRequest request, DonateRequest donateRequest, User user)
            throws Exception {
        Donation donation = new Donation();
        donation.setCampaign(campaignService.getCampaign(donateRequest.getCampaign_id()));
        donation.setAmount(donateRequest.getDonor_amount());
        donation.setDonorEmail(donateRequest.getDonor_email());
        donation.setDonorName(donateRequest.getDonor_name());
        donation.setDonorPhone(donateRequest.getDonor_phone());
        donation.setMessage(donateRequest.getMessage());
        donation.setIsAnonymous(donateRequest.isAnonymous());
        donation.setOrderId(VnpayUtil.getRandomNumber(8));
        donation.setPaymentMethod(donateRequest.getPaymentMethod());
        if (user == null) {
        } else {
            donation.setDonor(user);
        }
        Donation newDonation = donationRepository.save(donation);
        if (donateRequest.getPaymentMethod().equals(PaymentMethod.VNPAY)) {
            return vnPayService.createPaymentUrl(request, newDonation.getId(), newDonation.getAmount(),
                    newDonation.getOrderId());
        } else {
            return momoService.createPayment(newDonation.getAmount(),
                    newDonation.getOrderId());
        }
    }

    // @Override
    // public boolean updateDonate(String donateId, PaymentRequest paymentRequest) {
    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    // LocalDateTime localDateTime =
    // LocalDateTime.parse(paymentRequest.getPayDate(), formatter);
    // Donation donation = donationRepository.findByOrderId(donateId);
    // if (donation != null) {
    // donation.setTransactionId(paymentRequest.getTransactionId());
    // donation.setBankCode(paymentRequest.getBankCode());
    // donation.setResponseCode(paymentRequest.getResponseCode());
    // donation.setPaymentDate(localDateTime);
    // if (!paymentRequest.getResponseCode().equals("00")) {
    // donation.setPaymentStatus(PaymentStatus.FAILED);
    // donationRepository.save(donation);
    // return false;
    // } else {
    // donation.setPaymentStatus(PaymentStatus.COMPLETED);
    // donationRepository.save(donation);

    // DonationSuccessEventPayload eventPayload = new DonationSuccessEventPayload();
    // eventPayload.setDonateId(donation.getId());
    // eventPayload.setCampaignId(donation.getCampaign().getId());
    // eventPayload.setUserId(donation.getDonor() != null ?
    // donation.getDonor().getId() : null);
    // eventPayload.setAmount(donation.getAmount());
    // eventPayload.setDonorEmail(donation.getDonorEmail());
    // eventPayload.setDonorName(donation.getDonorName());
    // eventPayload.setProjectName(donation.getCampaign().getTitle());
    // eventPayload.setTransactionCode(donation.getTransactionId());

    // rabbitTemplate.convertAndSend(
    // RabbitMQConfig.EXCHANGE_DONATION_SUCCESS,
    // "",
    // eventPayload);

    // return true;
    // }

    // } else {
    // return false;
    // }
    // }

    @Override
    public VnPayIpnResponse updateDonateVnPay(Map<String, String> params) {
        try {
            // --- 1. KIỂM TRA CHECKSUM ---
            String vnp_SecureHash = params.get("vnp_SecureHash");

            // Loại bỏ 2 trường hash để tính toán lại
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            // Tính lại hash (Giả sử bạn đã có hàm này trong Util)
            String signValue = VnpayUtil.hashAllFields(params, vnpayConfig.getHashSecret());

            if (!signValue.equals(vnp_SecureHash)) {
                return new VnPayIpnResponse("97", "Invalid Checksum");
            }

            // --- 2. LẤY DỮ LIỆU ---
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_Amount = params.get("vnp_Amount");
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TransactionNo = params.get("vnp_TransactionNo");
            String vnp_BankCode = params.get("vnp_BankCode");

            // --- 3. TÌM ĐƠN HÀNG (Check Order Id) ---
            Donation donation = donationRepository.findByOrderId(vnp_TxnRef);
            if (donation == null) {
                return new VnPayIpnResponse("01", "Order not Found");
            }

            // --- 4. KIỂM TRA SỐ TIỀN (Check Amount) ---
            long amountVnpay = Long.parseLong(vnp_Amount) / 100;
            if (donation.getAmount().longValue() != amountVnpay) {
                return new VnPayIpnResponse("04", "Invalid Amount");
            }

            // --- 5. KIỂM TRA TRẠNG THÁI (Check Status - Idempotency) ---
            if (donation.getPaymentStatus() != PaymentStatus.PENDING) {
                return new VnPayIpnResponse("02", "Order already confirmed");
            }

            // --- 6. XỬ LÝ KẾT QUẢ & UPDATE DB ---
            if ("00".equals(vnp_ResponseCode)) {
                donation.setPaymentStatus(PaymentStatus.COMPLETED);
                donation.setTransactionId(vnp_TransactionNo);
                donation.setPaymentDate(LocalDateTime.now());
                System.out.println("VNPAY SUCCESS: Order " + vnp_TxnRef);
            } else {
                donation.setPaymentStatus(PaymentStatus.FAILED);
                System.out.println("VNPAY FAILED: Order " + vnp_TxnRef);
            }

            donation.setResponseCode(vnp_ResponseCode);

            // Lưu lại toàn bộ params để đối soát sau này
            donation.setPaymentDetails(new ObjectMapper().writeValueAsString(params));
            donation.setBankCode(vnp_BankCode);
            donationRepository.save(donation);

            DonationSuccessEventPayload eventPayload = new DonationSuccessEventPayload();
            eventPayload.setDonateId(donation.getId());
            eventPayload.setCampaignId(donation.getCampaign().getId());
            eventPayload.setUserId(donation.getDonor() != null ? donation.getDonor().getId() : null);
            eventPayload.setAmount(donation.getAmount());
            eventPayload.setDonorEmail(donation.getDonorEmail());
            eventPayload.setDonorName(donation.getDonorName());
            eventPayload.setProjectName(donation.getCampaign().getTitle());
            eventPayload.setTransactionCode(donation.getTransactionId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_DONATION_SUCCESS,
                    "",
                    eventPayload);

            return new VnPayIpnResponse("00", "Confirm Success");

        } catch (Exception e) {
            e.printStackTrace();
            return new VnPayIpnResponse("99", "Unknown error");
        }
    }

    @Override
    public void updateDonateMomo(MoMoIPN ipnData) throws Exception {
        String rawData = String.format(
                "accessKey=%s&amount=%d&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%d&resultCode=%d&transId=%d",
                momoConfig.getAccessKey(),
                ipnData.getAmount(),
                ipnData.getExtraData(),
                ipnData.getMessage(),
                ipnData.getOrderId(),
                ipnData.getOrderInfo(),
                ipnData.getOrderType(),
                ipnData.getPartnerCode(),
                ipnData.getPayType(),
                ipnData.getRequestId(),
                ipnData.getResponseTime(),
                ipnData.getResultCode(),
                ipnData.getTransId());

        String calculatedSignature = MomoUtil.signHmacSHA256(rawData, momoConfig.getSecretKey());

        if (!calculatedSignature.equals(ipnData.getSignature())) {
            throw new SecurityException("Invalid Signature: Chữ ký không khớp!");
        }

        // --- 2. TÌM ĐƠN HÀNG ---
        Donation donation = donationRepository.findByOrderId(ipnData.getOrderId());
        if (donation == null) {
            throw new Exception("Order not found: " + ipnData.getOrderId());
        }

        // --- 3. KIỂM TRA SỐ TIỀN ---
        // DB là BigDecimal, MoMo là Long
        if (donation.getAmount().longValue() != ipnData.getAmount()) {
            throw new Exception("Invalid Amount: Số tiền không khớp!");
        }

        // --- 4. CẬP NHẬT TRẠNG THÁI (Idempotency) ---
        if (donation.getPaymentStatus() == PaymentStatus.PENDING) {
            if (ipnData.getResultCode() == 0) {
                // THÀNH CÔNG
                donation.setPaymentStatus(PaymentStatus.COMPLETED);
                donation.setTransactionId(String.valueOf(ipnData.getTransId()));
                donation.setPaymentDate(LocalDateTime.now());
                log.info("MOMO SUCCESS: Order {}", ipnData.getOrderId());
            } else {
                // THẤT BẠI
                donation.setPaymentStatus(PaymentStatus.FAILED);
                log.info("MOMO FAILED: Order {}. Code: {}", ipnData.getOrderId(), ipnData.getResultCode());
            }

            donation.setBankCode(ipnData.getPayType());
            donation.setResponseCode(String.valueOf(ipnData.getResultCode()));
            // Lưu JSON log
            donation.setPaymentDetails(new ObjectMapper().writeValueAsString(ipnData));

            donationRepository.save(donation);
            DonationSuccessEventPayload eventPayload = new DonationSuccessEventPayload();
            eventPayload.setDonateId(donation.getId());
            eventPayload.setCampaignId(donation.getCampaign().getId());
            eventPayload.setUserId(donation.getDonor() != null ? donation.getDonor().getId() : null);
            eventPayload.setAmount(donation.getAmount());
            eventPayload.setDonorEmail(donation.getDonorEmail());
            eventPayload.setDonorName(donation.getDonorName());
            eventPayload.setProjectName(donation.getCampaign().getTitle());
            eventPayload.setTransactionCode(donation.getTransactionId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_DONATION_SUCCESS,
                    "",
                    eventPayload);

        } else {
            log.info("MOMO INFO: Order {} already processed.", ipnData.getOrderId());
        }
    }

    public boolean verifyUpdate(String id) {
        Donation donation = donationRepository.findByOrderId(id);
        if (donation == null) {
            return false;
        }
        if(!donation.getPaymentStatus().equals(PaymentStatus.COMPLETED)) {
            return false;
        }
        return true;
    }
}
