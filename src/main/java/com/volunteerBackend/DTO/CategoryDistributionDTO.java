package com.volunteerBackend.DTO;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDistributionDTO {
     private String name;
    private BigDecimal amount;
    private Double value; // percentage
}
