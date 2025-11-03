package com.volunteerBackend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.model.Category;
import com.volunteerBackend.model.Organizer;
import com.volunteerBackend.type.CampaignStatus;
import com.volunteerBackend.type.PaymentStatus;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByTitleContainingIgnoreCase(String title);
    boolean existsByTitle(String title);
    Campaign findByIdAndStatus( Long id, CampaignStatus status);
    List<Campaign> findByOrganizerAndStatus( Organizer organizer, CampaignStatus status);
    List<Campaign>findByStatusOrTitle(CampaignStatus status, String title);
    List<Campaign>findByCategoryAndStatusOrTitle(Category category, CampaignStatus status, String title);

    @Query("SELECT c FROM Campaign c WHERE c.status IN ('IN_PROGRESS', 'TARGET_REACHED', 'PAUSED') AND c.endDate < :currentDate")
    List<Campaign> findActiveCampaignsPastEndDate(LocalDate currentDate);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.campaign.id = :campaignId AND d.paymentStatus = :status")
    BigDecimal sumCompletedDonationsByCampaign(
        @Param("campaignId") Long campaignId,
        @Param("status") PaymentStatus status
    );

    @Query(value = """
        SELECT
            ROUND(
                CASE
                    WHEN c.target_amount > 0 THEN
                        (IFNULL(SUM(d.amount), 0) / c.target_amount) * 100
                    ELSE 0
                END
            )
        FROM
            campaigns c
        LEFT JOIN
            donations d ON c.id = d.campaign_id AND d.payment_status = 'COMPLETED'
        WHERE
            c.id = :campaignId
        GROUP BY
            c.id, c.target_amount
    """, nativeQuery = true)
    Integer getCaculatePercentage(@Param("campaignId") Long campaignId);


    // Đếm chiến dịch đang active
    @Query(value = """
        SELECT COUNT(*) 
        FROM campaigns 
        WHERE status = 'IN_PROGRESS'
        AND (end_date IS NULL OR end_date >= CURDATE())
        """, nativeQuery = true)
    Integer countActiveCampaigns();
    
    // Tính tỷ lệ hoàn thành trung bình
    @Query(value = """
        SELECT
            ROUND(AVG(campaign_rates.completion_percentage), 2) AS avgCompletionRate
        FROM (
            SELECT
                (COALESCE(SUM(d.amount), 0) / c.target_amount * 100) AS completion_percentage
            FROM
                campaigns c
            LEFT JOIN
                donations d ON d.campaign_id = c.id AND d.payment_status = 'COMPLETED'
            WHERE
                c.status IN ('IN_PROGRESS', 'TARGET_REACHED') AND c.target_amount > 0
            GROUP BY
                c.id, c.target_amount
        ) AS campaign_rates
    """, nativeQuery = true)
    Double calculateAverageCompletionRate();
    
    // Top 4 chiến dịch hot nhất
    @Query(value = """
        SELECT 
            c.id,
            c.title as name,
            COALESCE(SUM(d.amount), 0) as raised,
            c.target_amount as goal,
            COUNT(DISTINCT d.id) as donorCount,
            ROUND(
                CASE 
                    WHEN c.target_amount > 0 
                    THEN COALESCE(SUM(d.amount), 0) / c.target_amount * 100
                    ELSE 0 
                END, 
                2
            ) as progress
        FROM campaigns c
        LEFT JOIN donations d ON d.campaign_id = c.id 
            AND d.payment_status = 'COMPLETED'
        WHERE c.status = 'IN_PROGRESS'
        GROUP BY c.id, c.title, c.target_amount
        ORDER BY raised DESC
        LIMIT 4
        """, nativeQuery = true)
    List<Object[]> getTopCampaigns();
}