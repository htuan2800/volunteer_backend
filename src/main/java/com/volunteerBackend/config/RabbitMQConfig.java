package com.volunteerBackend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
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
    public static final String QUEUE_FORGETPASSWORD_NAME = "user.forgetpassword.send_verification_email.queue";
    public static final String ROUTING_FORGETPASSWORD_KEY = "user.forgetpassword.verify_email";

    // --- Constants for News Notification ---
    public static final String EXCHANGE_NOTIFICATION_NAME = "notification.exchange";
    public static final String QUEUE_NOTIFICATION_CAMPAIGN = "notification.campaign.queue";
    public static final String QUEUE_NOTIFICATION_SYSTEM = "notification.system.queue";
    public static final String QUEUE_NOTIFICATION_DONATION = "notification.donation.queue";
    public static final String ROUTING_KEY_NOTIFICATION_CAMPAIGN = "notification.campaign";
    public static final String ROUTING_KEY_NOTIFICATION_SYSTEM = "notification.system";
    public static final String ROUTING_KEY_NOTIFICATION_DONATION = "notification.donation";

    // ====== BEANS FOR DONATION SUCCESS EVENT ======
    public static final String EXCHANGE_DONATION_SUCCESS = "donation.success.exchange";
    public static final String QUEUE_DONATION_SUCCESS_EMAIL = "donation.success.send_email.queue";
    public static final String QUEUE_DONATION_SUCCESS_NOTIFICATION = "donation.success.create_notification.queue";
    public static final String QUEUE_DONATION_SUCCESS_DASHBOARD = "donation.success.update_dashboard.queue";
    public static final String QUEUE_DONATION_SUCCESS_CAMPAIGN = "donation.success.update_campaign.queue";


    // ====== BEANS FOR UPDATE CAMPAIGN STATUS EVENT ======
    public static final String EXCHANGE_CAMPAIGN_STATUS= "campaign.status.exchange";
    public static final String QUEUE_CAMPAIGN_STATUS_NOTIFICATION = "campaign.status.create_notification.queue";
    public static final String QUEUE_CAMPAIGN_STATUS_CAMPAIGN = "campaign.status.update_campaign.queue";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE_USER_NAME);
    }

    @Bean
    public Queue registrationQueue() {
        return new Queue(QUEUE_REGISTRATION_NAME, true);
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

    // ====== BEANS FOR DONATION SUCCESS EVENT ======
    @Bean
    public FanoutExchange donationSuccessExchange() {
        return new FanoutExchange(EXCHANGE_DONATION_SUCCESS);
    }

    @Bean
    public Queue donationSuccessEmailQueue() {
        return new Queue(QUEUE_DONATION_SUCCESS_EMAIL, true);
    }

    @Bean
    public Queue donationSuccessNotificationQueue() {
        return new Queue(QUEUE_DONATION_SUCCESS_NOTIFICATION, true);
    }

    @Bean
    public Queue donationSuccessDashboardQueue() {
        return new Queue(QUEUE_DONATION_SUCCESS_DASHBOARD, true);
    }

    @Bean
    public Queue donationSuccessCampaignQueue() {
        return new Queue(QUEUE_DONATION_SUCCESS_CAMPAIGN, true);
    }

    @Bean
    public Binding donationEmailBinding(Queue donationSuccessEmailQueue, FanoutExchange donationSuccessExchange) {
        return BindingBuilder.bind(donationSuccessEmailQueue).to(donationSuccessExchange);
    }

    @Bean
    public Binding donationNotificationBinding(Queue donationSuccessNotificationQueue,
            FanoutExchange donationSuccessExchange) {
        return BindingBuilder.bind(donationSuccessNotificationQueue).to(donationSuccessExchange);
    }

    @Bean
    public Binding donationDashboardBinding(Queue donationSuccessDashboardQueue,
            FanoutExchange donationSuccessExchange) {
        return BindingBuilder.bind(donationSuccessDashboardQueue).to(donationSuccessExchange);
    }

    @Bean
    public Binding donationCampaignBinding(Queue donationSuccessCampaignQueue,
            FanoutExchange donationSuccessExchange) {
        return BindingBuilder.bind(donationSuccessCampaignQueue).to(donationSuccessExchange);
    }

    // ====== BEANS FOR UPDATE CAMPAIGN SUCCESS EVENT ======
    @Bean
    public FanoutExchange campaignStatusExchange() {
        return new FanoutExchange(EXCHANGE_CAMPAIGN_STATUS);
    }

    @Bean
    public Queue campaignStatusNotificationQueue() {
        return new Queue(QUEUE_CAMPAIGN_STATUS_NOTIFICATION, true);
    }

    @Bean
    public Queue campaignStatusCampaignQueue() {
        return new Queue(QUEUE_CAMPAIGN_STATUS_CAMPAIGN, true);
    }

    @Bean
    public Binding campaignNotificationBinding(Queue campaignStatusNotificationQueue,
            FanoutExchange campaignStatusExchange) {
        return BindingBuilder.bind(campaignStatusNotificationQueue).to(campaignStatusExchange);
    }

    @Bean
    public Binding campaignCampaignBinding(Queue campaignStatusCampaignQueue,
            FanoutExchange campaignStatusExchange) {
        return BindingBuilder.bind(campaignStatusCampaignQueue).to(campaignStatusExchange);
    }

    // Giúp RabbitMQ tự động chuyển đổi object Java sang JSON và ngược lại
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory myFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
                                                     
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        
        factory.setMessageConverter(jsonMessageConverter); 
        
        return factory;
    }
}
