package com.volunteerBackend.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOfUserDTO {
    private BigDecimal totalDonations;
    private Long totalCampaigns;
}
