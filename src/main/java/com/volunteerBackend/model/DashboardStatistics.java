package com.volunteerBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
// Tên class nên theo chuẩn PascalCase của Java
public class DashboardStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Bắt buộc phải có
    private Integer totalOrganizers;

    @Column(name = "donations_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDonationsAmount;
    
    private Integer totalUsers;
    private Integer totalCampaigns; 
    private Integer totalSupportCount;
    private LocalDateTime upDateTime;
}