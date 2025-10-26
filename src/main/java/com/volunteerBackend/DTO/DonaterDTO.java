package com.volunteerBackend.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.volunteerBackend.type.PaymentMethod;
import com.volunteerBackend.type.PaymentStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DonaterDTO {
    private Long id;
    private String donorName;
    private String donorEmail;
    private String donorPhone;
    private BigDecimal amount;
    private String message;
    private Boolean isAnonymous;
    private PaymentMethod paymentMethod;
    private String vnpTransactionNo;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime paymentDate;
}
