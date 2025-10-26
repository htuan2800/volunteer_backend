package com.volunteerBackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDTO {
    private Integer totalOrganizers;
    private Integer totalDonationsAmount;
    private Integer totalUsers;
    private Integer totalCampaigns;
    private Integer totalSupportCount;
}
