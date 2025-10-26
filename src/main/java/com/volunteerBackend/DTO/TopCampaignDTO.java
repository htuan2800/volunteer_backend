package com.volunteerBackend.DTO;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopCampaignDTO {
    private Long id;
    private String name;
    private BigDecimal raised;
    private BigDecimal goal;
    private Integer donors;
    private Double progress;
}
