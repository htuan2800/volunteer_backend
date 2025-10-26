package com.volunteerBackend.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentActivityDTO {
    private Long id;
    private String donorName;
    private BigDecimal amount;
    private String campaignTitle;
    private LocalDateTime paymentDate;
}
