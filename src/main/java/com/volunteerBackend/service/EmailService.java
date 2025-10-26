package com.volunteerBackend.service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import com.volunteerBackend.payload.EmailResetPayload;
import com.volunteerBackend.payload.EmailVerifyPayload;

public interface EmailService {
    public void sendVerificationEmail(String to, String token, String fullName);
    public CompletableFuture<Void> sendVerificationEmailWithAsync(EmailVerifyPayload payload);
    public void sendDonationThankYouEmail(String to, String fullName, BigDecimal amount, String projectName, String transactionCode);
    public void sendForgotPasswordEmail(EmailResetPayload payload);
}
