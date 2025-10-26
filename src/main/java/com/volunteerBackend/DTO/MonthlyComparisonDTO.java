package com.volunteerBackend.DTO;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyComparisonDTO {
    private String month;
    private BigDecimal donations;
    private Integer campaigns;
}
