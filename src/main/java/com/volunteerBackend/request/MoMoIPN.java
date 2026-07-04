package com.volunteerBackend.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoMoIPN {
    private String orderType;
    private Long amount;
    private String partnerCode;
    private String orderId;
    private String extraData;
    private String signature;
    private Long transId;
    private Long responseTime;
    private Integer resultCode;
    private String message;
    private String payType;
    private String requestId;
    private String orderInfo;
}
