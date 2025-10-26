package com.volunteerBackend.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.volunteerBackend.model.Donation;
import com.volunteerBackend.model.User;
import com.volunteerBackend.request.DonateRequest;
import com.volunteerBackend.request.PaymentRequest;

import jakarta.servlet.http.HttpServletRequest;

public interface DonateService {
    List<Donation> getAllDonates();
    List<Donation> getAllDonatesByDonorName(String donorName);
    List<Donation> getAllDonatesByCampaign(Long campaignID);
    Donation getDonationById(Long id);
    String createDonate(HttpServletRequest request, DonateRequest donateRequest, User user) throws UnsupportedEncodingException;
    String updateDonate(String donateId, PaymentRequest paymentRequest);

}
