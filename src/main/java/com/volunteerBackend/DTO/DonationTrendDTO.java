package com.volunteerBackend.DTO;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DonationTrendDTO {
    private String date;
    private BigDecimal amount;
    private Integer donors;
}
