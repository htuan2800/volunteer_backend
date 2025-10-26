package com.volunteerBackend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.volunteerBackend.DTO.CategoryDistributionDTO;
import com.volunteerBackend.DTO.DashboardStatisticsDTO;
import com.volunteerBackend.DTO.DonationTrendDTO;
import com.volunteerBackend.DTO.MonthlyComparisonDTO;
import com.volunteerBackend.DTO.RecentActivityDTO;
import com.volunteerBackend.DTO.StatsOverviewDTO;
import com.volunteerBackend.DTO.TopCampaignDTO;
import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.model.DashboardStatistics;
import com.volunteerBackend.model.Notification.NotificationType;
import com.volunteerBackend.repository.CampaignRepository;
import com.volunteerBackend.repository.DashboardStatisticsRepository;
import com.volunteerBackend.repository.DonationRepository;
import com.volunteerBackend.request.NotificationRequest;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardStatisticsServiceImp implements DashboardStatisticsService {

    private final DashboardStatisticsRepository dashboardStatisticsRepository;

    private final CampaignRepository campaignRepository;

    private final NotificationService notificationService;

    private final DonationRepository donationRepository;

    @Override
    public DashboardStatistics getDashboardStatistics() {
        return dashboardStatisticsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Statistics not found!"));
    }

    @Override
    public boolean updateTotalCampaigns() {
        // Luôn tìm đến bản ghi có ID là 1
        DashboardStatistics stats = dashboardStatisticsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Statistics not found!"));
        // Cập nhật giá trị
        stats.setTotalCampaigns(stats.getTotalCampaigns() + 1);
        stats.setUpDateTime(LocalDateTime.now());
        // Lưu lại
        dashboardStatisticsRepository.save(stats);
        return true;
    }

    @Override
    public boolean updateTotalDonationsAmount(BigDecimal amount) {
        // Luôn tìm đến bản ghi có ID là 1
        DashboardStatistics stats = dashboardStatisticsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Statistics not found!"));
        // Cập nhật giá trị
        stats.setTotalDonationsAmount(stats.getTotalDonationsAmount().add(amount));
        stats.setUpDateTime(LocalDateTime.now());
        // Lưu lại
        dashboardStatisticsRepository.save(stats);
        return true;
    }

    @Override
    public boolean updateTotalSupportCount() {
        // Luôn tìm đến bản ghi có ID là 1
        DashboardStatistics stats = dashboardStatisticsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Statistics not found!"));
        // Cập nhật giá trị
        stats.setTotalSupportCount(stats.getTotalSupportCount() + 1);
        stats.setUpDateTime(LocalDateTime.now());
        // Lưu lại
        dashboardStatisticsRepository.save(stats);
        return true;
    }

    @Override
    public boolean updateTotalOrganizers() {
        // Luôn tìm đến bản ghi có ID là 1
        DashboardStatistics stats = dashboardStatisticsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Statistics not found!"));
        // Cập nhật giá trị
        stats.setTotalOrganizers(stats.getTotalOrganizers() + 1);
        stats.setUpDateTime(LocalDateTime.now());
        // Lưu lại
        dashboardStatisticsRepository.save(stats);
        return true;
    }

    @Override
    public boolean updateTotalUsers() {
        // Luôn tìm đến bản ghi có ID là 1
        DashboardStatistics stats = dashboardStatisticsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Statistics not found!"));
        // Cập nhật giá trị
        stats.setTotalUsers(stats.getTotalUsers() + 1);
        stats.setUpDateTime(LocalDateTime.now());
        // Lưu lại
        dashboardStatisticsRepository.save(stats);
        return true;
    }

    @Override
    public void getCaculatePercentage(Long campaignID) {
        Campaign campaign = campaignRepository.findById(campaignID)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chiến dịch với ID: " + campaignID));

        Integer percentage = campaignRepository.getCaculatePercentage(campaignID);
        if (percentage == null || percentage < 100) {
            return;
        }

        Set<Integer> uniqueDonorIds = campaign.getDonations().stream() // Chuyển list thành stream
                .map(donation -> donation.getDonor().getId()) // Lấy ra ID của người quyên góp
                .collect(Collectors.toSet()); // Gom lại thành một Set để loại bỏ các ID trùng lặp

        for (Integer donorId : uniqueDonorIds) {
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle(campaign.getTitle());
            notificationRequest.setMessage("Chiến dịch '" + campaign.getTitle()
                    + "' bạn tham gia đã hoàn thành 100%. Cảm ơn sự đóng góp của bạn!");
            notificationRequest.setRelatedId(campaign.getId());
            notificationRequest.setType(NotificationType.CAMPAIGN);
            notificationRequest.setUserId(donorId);

            notificationService.createNotification(notificationRequest);
        }
    }

    @Override
    public DashboardStatisticsDTO getStatistics(int days) {
        System.out.println("days: " + days);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        // Tính toán stats với kỳ so sánh
        LocalDateTime prevStartDate = startDate.minusDays(days);

        return DashboardStatisticsDTO.builder()
                .stats(calculateStats(startDate, endDate, prevStartDate))
                .donationTrend(getDonationTrend(startDate))
                .topCampaigns(getTopCampaigns())
                .donationByCategory(getCategoryDistribution())
                .monthlyComparison(getMonthlyComparison())
                .recentActivities(getRecentActivities())
                .build();
    }

    private StatsOverviewDTO calculateStats(LocalDateTime start, LocalDateTime end, LocalDateTime prevStart) {
        // Current period
        BigDecimal totalDonations = donationRepository.sumTotalByDateRange(start, end);
        Integer activeCampaigns = campaignRepository.countActiveCampaigns();
        Integer totalDonors = donationRepository.countUniqueDonors(start, end);
        Double completionRate = Optional.ofNullable(
                campaignRepository.calculateAverageCompletionRate()).orElse(0.0);

        // Previous period for comparison
        BigDecimal prevDonations = donationRepository.sumTotalByDateRange(prevStart, start);
        Integer prevDonors = donationRepository.countUniqueDonors(prevStart, start);

        return StatsOverviewDTO.builder()
                .totalDonations(totalDonations)
                .totalDonationsChange(calculateChange(prevDonations, totalDonations))
                .totalDonationsTrend(getTrend(prevDonations, totalDonations))

                .activeCampaigns(activeCampaigns)
                .activeCampaignsChange(String.valueOf(activeCampaigns))
                .activeCampaignsTrend("up")

                .totalDonors(totalDonors)
                .totalDonorsChange(calculateChange(
                        BigDecimal.valueOf(prevDonors),
                        BigDecimal.valueOf(totalDonors)))
                .totalDonorsTrend(getTrend(
                        BigDecimal.valueOf(prevDonors),
                        BigDecimal.valueOf(totalDonors)))

                .completionRate(completionRate)
                .completionRateChange(String.format("%.1f%%", completionRate))
                .completionRateTrend("up")
                .build();
    }

    private List<DonationTrendDTO> getDonationTrend(LocalDateTime startDate) {
        List<Object[]> results = donationRepository.getDonationTrendByDate(startDate);

        return results.stream()
                .map(row -> DonationTrendDTO.builder()
                        .date(row[0].toString())
                        .amount(new BigDecimal(row[1].toString()))
                        .donors(((Number) row[2]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TopCampaignDTO> getTopCampaigns() {
        List<Object[]> results = campaignRepository.getTopCampaigns();

        return results.stream()
                .map(row -> TopCampaignDTO.builder()
                        .id(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .raised(new BigDecimal(row[2].toString()))
                        .goal(new BigDecimal(row[3].toString()))
                        .donors(((Number) row[4]).intValue())
                        .progress(((Number) row[5]).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CategoryDistributionDTO> getCategoryDistribution() {
        List<Object[]> results = donationRepository.getDonationByCategory();

        return results.stream()
                .map(row -> CategoryDistributionDTO.builder()
                        .name((String) row[0])
                        .amount(new BigDecimal(row[2].toString()))
                        .value(((Number) row[3]).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<MonthlyComparisonDTO> getMonthlyComparison() {
        List<Object[]> results = donationRepository.getMonthlyComparison();

        return results.stream()
                .map(row -> MonthlyComparisonDTO.builder()
                        .month(formatMonth((String) row[0]))
                        .donations(new BigDecimal(row[1].toString()))
                        .campaigns(((Number) row[2]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<RecentActivityDTO> getRecentActivities() {
        List<Object[]> results = donationRepository.getRecentActivities();

        return results.stream()
                .map(row -> {
                    // Lấy ra đối tượng Timestamp từ mảng
                    java.sql.Timestamp paymentTimestamp = (java.sql.Timestamp) row[4];

                    return RecentActivityDTO.builder()
                            .id(((Number) row[0]).longValue())
                            .donorName((String) row[1])
                            .amount(new BigDecimal(row[2].toString()))
                            .campaignTitle((String) row[3])
                            // Sửa ở đây: Chuyển đổi Timestamp sang LocalDateTime
                            .paymentDate(paymentTimestamp.toLocalDateTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // Helper methods
    private String calculateChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return newValue.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0%";
        }

        BigDecimal change = newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return String.format("%+.1f%%", change.doubleValue());
    }

    private String getTrend(BigDecimal oldValue, BigDecimal newValue) {
        return newValue.compareTo(oldValue) >= 0 ? "up" : "down";
    }

    private String formatMonth(String yearMonth) {
        // "2024-10" -> "Tháng 10"
        String[] parts = yearMonth.split("-");
        return "Tháng " + Integer.parseInt(parts[1]);
    }
}
