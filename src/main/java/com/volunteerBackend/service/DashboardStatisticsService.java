package com.volunteerBackend.service;

import java.math.BigDecimal;
import com.volunteerBackend.DTO.DashboardStatisticsDTO;
import com.volunteerBackend.model.DashboardStatistics;

public interface DashboardStatisticsService {
    public DashboardStatistics getDashboardStatistics();
    public boolean updateTotalCampaigns();
    public boolean updateTotalSupportCount();
    public boolean updateTotalDonationsAmount(BigDecimal amount);
    public boolean updateTotalOrganizers();
    public boolean updateTotalUsers();
    public void getCaculatePercentage(Long campaignID);
    public DashboardStatisticsDTO getStatistics(int days);
}
