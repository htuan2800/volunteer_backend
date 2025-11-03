package com.volunteerBackend.payload;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Implement Serializable để đảm bảo có thể truyền qua message queue an toàn
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonationSuccessEventPayload implements Serializable {
    private Long donateId;
    private Long campaignId;
    private Integer userId;
    private String donorEmail;
    private String donorName;
    private BigDecimal amount;
    private String projectName;
    private String transactionCode;
}