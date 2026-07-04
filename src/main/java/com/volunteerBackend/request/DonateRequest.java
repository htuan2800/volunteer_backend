package com.volunteerBackend.request;

import java.math.BigDecimal;

import com.volunteerBackend.type.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonateRequest {
    private Long campaign_id;
    private boolean anonymous;
    private BigDecimal donor_amount;
    private String donor_email;
    private String donor_name;
    private String donor_phone;
    private String message;
    private PaymentMethod paymentMethod;
}
