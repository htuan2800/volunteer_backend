package com.volunteerBackend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.model.Donation;
import com.volunteerBackend.model.User;
import com.volunteerBackend.type.PaymentStatus;


public interface DonationRepository extends JpaRepository<Donation, Long> {
    Donation findByOrderId(String orderId);

    List<Donation> findByDonorNameContainingIgnoreCase(String donorName);

    List<Donation> findByCampaignIdAndPaymentStatus(Long campaignId, PaymentStatus paymentStatus);

     // Tổng quyên góp theo khoảng thời gian
    @Query(value = """
        SELECT COALESCE(SUM(d.amount), 0) 
        FROM donations d 
        WHERE d.payment_status = 'COMPLETED' 
        AND d.payment_date BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    BigDecimal sumTotalByDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    // Đếm số người ủng hộ unique
    @Query(value = """
        SELECT COUNT(DISTINCT CASE 
            WHEN d.donor_id IS NOT NULL THEN d.donor_id 
            ELSE d.donor_email 
        END) 
        FROM donations d 
        WHERE d.payment_status = 'COMPLETED' 
        AND d.payment_date BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    Integer countUniqueDonors(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    // Xu hướng quyên góp theo ngày (7 ngày gần nhất)
    @Query(value = """
        SELECT 
            DATE(d.payment_date) as date,
            COALESCE(SUM(d.amount), 0) as amount,
            COUNT(*) as donors
        FROM donations d
        WHERE d.payment_status = 'COMPLETED'
        AND d.payment_date >= :startDate
        GROUP BY DATE(d.payment_date)
        ORDER BY date ASC
        """, nativeQuery = true)
    List<Object[]> getDonationTrendByDate(@Param("startDate") LocalDateTime startDate);
    
    // Quyên góp theo category
    @Query(value = """
        SELECT 
            cat.name as categoryName,
            COUNT(DISTINCT c.id) as campaignCount,
            COALESCE(SUM(d.amount), 0) as totalAmount,
            ROUND(
                COALESCE(SUM(d.amount), 0) * 100.0 / 
                (SELECT SUM(amount) FROM donations WHERE payment_status = 'COMPLETED'), 
                2
            ) as percentage
        FROM categories cat
        LEFT JOIN campaigns c ON c.category_id = cat.id 
            AND c.status IN ('IN_PROGRESS', 'TARGET_REACHED')
        LEFT JOIN donations d ON d.campaign_id = c.id 
            AND d.payment_status = 'COMPLETED'
        WHERE cat.is_active = true 
        AND cat.is_deleted = false
        GROUP BY cat.id, cat.name
        HAVING COALESCE(SUM(d.amount), 0) > 0
        ORDER BY totalAmount DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> getDonationByCategory();
    
    // So sánh theo tháng (5 tháng gần nhất)
    @Query(value = """
        SELECT 
            DATE_FORMAT(d.payment_date, '%Y-%m') as month,
            COALESCE(SUM(d.amount), 0) as donations,
            COUNT(DISTINCT d.campaign_id) as campaigns
        FROM donations d
        WHERE d.payment_status = 'COMPLETED'
        AND d.payment_date >= DATE_SUB(CURDATE(), INTERVAL 5 MONTH)
        GROUP BY DATE_FORMAT(d.payment_date, '%Y-%m')
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> getMonthlyComparison();
    
    // Hoạt động gần đây (10 donations mới nhất)
    @Query(value = """
        SELECT 
            d.id,
            COALESCE(u.full_name, d.donor_name, 'Ẩn danh') as donorName,
            d.amount,
            c.title as campaignTitle,
            d.payment_date
        FROM donations d
        LEFT JOIN users u ON d.donor_id = u.id
        INNER JOIN campaigns c ON d.campaign_id = c.id
        WHERE d.payment_status = 'COMPLETED'
        ORDER BY d.payment_date DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> getRecentActivities();

    @Query(value = """
        SELECT COALESCE(SUM(d.amount), 0) 
        FROM donations d 
        WHERE d.payment_status = 'COMPLETED' 
        AND d.donor_id = :userId
        """, nativeQuery = true)
    BigDecimal sumAmountDonationOfUser (@Param("userId") Integer userId);

    @Query("SELECT COUNT(DISTINCT d.campaign) FROM Donation d WHERE d.donor = :donor")
    Long countDistinctCampaignsForDonor(@Param("donor") User donor);

    @Query("SELECT DISTINCT d.campaign FROM Donation d WHERE d.donor = :donor AND d.paymentStatus = :status")
    List<Campaign> findDistinctCampaignsByDonorAndPaymentStatus(
        @Param("donor") User donor,
        @Param("status") PaymentStatus status
    );

    @Query("SELECT DISTINCT d.donor FROM Donation d WHERE d.campaign.id = :campaignId")
    List<User> findDistincUsersByCampaignId(Long campaignId);
}
