package com.volunteerBackend.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String vnp_TxnRef;
    private String vnp_TransactionNo;
    private String vnp_BankTranNo;
    private String vnp_BankCode;
    private String vnp_ResponseCode;
    private String vnp_PayDate;
}
