package com.volunteerBackend.service.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.volunteerBackend.config.RabbitMQConfig;
import com.volunteerBackend.payload.EmailResetPayload;
import com.volunteerBackend.payload.EmailVerifyPayload;
import com.volunteerBackend.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserConsumer {
    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REGISTRATION_NAME)
    public void handleRegistrationEmail(EmailVerifyPayload payload) {
        System.out.println("LOG: Received email request from RabbitMQ. Preparing to send to: " + payload);
        try {
            emailService.sendVerificationEmailWithAsync(
                    payload).exceptionally(ex -> {
                        log.error("Email sending failed for user: " + payload.getEmail(), ex);
                        return null;
                    });
        } catch (Exception e) {
            System.err.println(
                    "ERROR: Consumer failed to process email for " + payload + ". Reason: " + e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_FORGETPASSWORD_NAME)
    public void handleForgotPasswordEmail(EmailResetPayload payload) {
        System.out.println("LOG: Received email request from RabbitMQ. Preparing to send to: " + payload);
        try {
            emailService.sendForgotPasswordEmail(payload);
        } catch (Exception e) {
            System.err.println(
                    "ERROR: Consumer failed to process email for " + payload + ". Reason: " + e.getMessage());
        }
    }
}
