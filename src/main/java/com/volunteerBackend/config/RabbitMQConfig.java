package com.volunteerBackend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // --- Constants for User Registration Email ---
    public static final String EXCHANGE_USER_NAME = "user.exchange";
    public static final String QUEUE_REGISTRATION_NAME = "user.registration.send_verification_email.queue";
    public static final String ROUTING_REGISTRATION_KEY = "user.registration.verify_email";
    public static final String QUEUE_FORGETPASSWORD_NAME="user.forgetpassword.send_verification_email.queue";
    public static final String ROUTING_FORGETPASSWORD_KEY="user.forgetpassword.verify_email";
    public static final String QUEUE_SENT_EMAIL_DONATION_SUCCESS = "user.donation.success.send_email.queue";
    public static final String ROUTING_KEY_SENT_EMAIL_DONATION_SUCCESS = "user.donation.success.email";

    // --- Constants for News Notification ---
    public static final String EXCHANGE_NOTIFICATION_NAME = "notification.exchange";
    // --- Định nghĩa các QUEUE riêng biệt ---
    public static final String QUEUE_NOTIFICATION_CAMPAIGN = "notification.campaign.queue";
    public static final String QUEUE_NOTIFICATION_SYSTEM = "notification.system.queue";
    public static final String QUEUE_NOTIFICATION_DONATION = "notification.donation.queue";
    public static final String ROUTING_KEY_NOTIFICATION_CAMPAIGN = "notification.campaign";
    public static final String ROUTING_KEY_NOTIFICATION_SYSTEM = "notification.system";
    public static final String ROUTING_KEY_NOTIFICATION_DONATION = "notification.donation";

    // ====== BEANS FOR USER REGISTRATION ======

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE_USER_NAME);
    }


    @Bean
    public Queue registrationQueue() {
        return new Queue(QUEUE_REGISTRATION_NAME, true);
    }

    @Bean
    public Queue sentEmailDonationSuccessQueue() {
        return new Queue(QUEUE_SENT_EMAIL_DONATION_SUCCESS, true);
    }

    @Bean
    public Queue forgetPasswordQueue() {
        return new Queue(QUEUE_FORGETPASSWORD_NAME, true);
    }

    @Bean
    public Binding registrationBinding(@Qualifier("registrationQueue") Queue queue,
            @Qualifier("userExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_REGISTRATION_KEY);
    }

    @Bean
    public Binding sentEmailDonationSuccessBinding(@Qualifier("sentEmailDonationSuccessQueue") Queue queue,
            @Qualifier("userExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_SENT_EMAIL_DONATION_SUCCESS);
    }

    @Bean
    public Binding forgetPasswordBinding(@Qualifier("forgetPasswordQueue") Queue queue,
            @Qualifier("userExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_FORGETPASSWORD_KEY);
    }

    // ====== BEANS FOR NEWS NOTIFICATION ======

    @Bean
    public Queue notificationCampaignQueue() {
        return new Queue(QUEUE_NOTIFICATION_CAMPAIGN, true);
    }

    @Bean
    public Queue notificationSystemQueue() {
        return new Queue(QUEUE_NOTIFICATION_SYSTEM, true);
    }

    @Bean
    public Queue notificationDonationQueue() {
        return new Queue(QUEUE_NOTIFICATION_DONATION, true);
    }

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(EXCHANGE_NOTIFICATION_NAME);
    }

    @Bean
    public Binding notificationCampaignBinding(@Qualifier("notificationCampaignQueue") Queue queue,
            @Qualifier("notificationsExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_NOTIFICATION_CAMPAIGN);
    }

    @Bean
    public Binding notificationSystemBinding(@Qualifier("notificationSystemQueue") Queue queue,
            @Qualifier("notificationsExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_NOTIFICATION_SYSTEM);
    }

    @Bean
    public Binding notificationDonationBinding(@Qualifier("notificationDonationQueue") Queue queue,
            @Qualifier("notificationsExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_NOTIFICATION_DONATION);
    }

    // ====== GENERAL BEAN ======

    // Bean này rất quan trọng: Giúp RabbitMQ tự động chuyển đổi object Java sang
    // JSON và ngược lại
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
