package com.volunteerBackend.request;

import java.math.BigDecimal;

import com.volunteerBackend.type.CampaignStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRequest {
    private Long id;
    private String title;
    private String storyInfo;
    private BigDecimal targetAmount;
    private CampaignStatus status;
    private Long category;
    private Integer organizer;
    private String featuredImage;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
}
