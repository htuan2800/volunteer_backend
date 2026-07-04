package com.volunteerBackend.service;
import java.util.List;
import java.util.Map;

import com.volunteerBackend.model.Donation;
import com.volunteerBackend.model.User;
import com.volunteerBackend.request.DonateRequest;
import com.volunteerBackend.request.MoMoIPN;
import com.volunteerBackend.response.VnPayIpnResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface DonateService {
    List<Donation> getAllDonates();
    List<Donation> getAllDonatesByDonorName(String donorName);
    List<Donation> getAllDonatesByCampaign(Long campaignID);
    List<User> getUsersByCampaign(Long campaignID);
    Donation getDonationById(Long id);
    String createDonate(HttpServletRequest request, DonateRequest donateRequest, User user) throws Exception;
    // boolean updateDonate(String donateId, PaymentRequest paymentRequest);
    VnPayIpnResponse updateDonateVnPay(Map<String, String> params);
    void updateDonateMomo(MoMoIPN ipnData) throws Exception;
    boolean verifyUpdate(String id);
}
