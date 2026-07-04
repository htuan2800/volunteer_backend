package com.volunteerBackend.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker for /topic
        config.enableSimpleBroker("/topic");
        // Application destination prefix for @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        // Optional: Set user destination prefix for sending to specific users
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint for clients
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:5175","https://49j386n7-5175.asse.devtunnels.ms").withSockJS(); // SockJS for fallback
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        messageConverters.add(new JacksonJsonMessageConverter());
        return false; // để Spring vẫn dùng thêm Jackson
    }
}