package com.volunteerBackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "momo")
public class MomoConfig {
    private String partnerCode;
    private String returnUrl;
    private String endPoint;
    private String ipnUrl;
    private String accessKey;
    private String secretKey;
    private String requestType;
}
