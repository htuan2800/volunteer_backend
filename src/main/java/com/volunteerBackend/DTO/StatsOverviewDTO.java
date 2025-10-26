package com.volunteerBackend.DTO;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatsOverviewDTO {
    private BigDecimal totalDonations;
    private String totalDonationsChange;
    private String totalDonationsTrend; // "up" or "down"
    
    private Integer activeCampaigns;
    private String activeCampaignsChange;
    private String activeCampaignsTrend;
    
    private Integer totalDonors;
    private String totalDonorsChange;
    private String totalDonorsTrend;
    
    private Double completionRate;
    private String completionRateChange;
    private String completionRateTrend;
}
