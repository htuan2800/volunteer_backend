package com.volunteerBackend.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerStatsDTO {
    private long totalCampaigns;
    private long totalDonations;
    private BigDecimal totalAmountRaised;
}
