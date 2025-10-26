package com.volunteerBackend.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.volunteerBackend.type.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonateSummaryDTO {
    private Long id;
    private String fullName;
    private PaymentStatus status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
