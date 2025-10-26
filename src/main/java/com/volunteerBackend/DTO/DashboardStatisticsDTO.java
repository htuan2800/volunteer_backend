package com.volunteerBackend.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatisticsDTO {
    private StatsOverviewDTO stats;
    private List<DonationTrendDTO> donationTrend;
    private List<TopCampaignDTO> topCampaigns;
    private List<CategoryDistributionDTO> donationByCategory;
    private List<MonthlyComparisonDTO> monthlyComparison;
    private List<RecentActivityDTO> recentActivities;
}
