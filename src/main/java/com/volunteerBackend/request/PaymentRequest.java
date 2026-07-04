package com.volunteerBackend.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String orderId;
    private String transactionId;
    private String bankCode;
    private String ResponseCode;
    private String payDate;
}
