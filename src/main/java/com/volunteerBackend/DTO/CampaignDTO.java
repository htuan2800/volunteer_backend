package com.volunteerBackend.DTO;

import java.math.BigDecimal;
import java.util.List;

import com.volunteerBackend.type.CampaignStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDTO {
    public Long campaignId;
    public String title;
    private BigDecimal targetAmount;
    public String featuredImage;
    public CampaignStatus status;
    public java.time.LocalDate startDate;
    public java.time.LocalDate endDate;
    public String storyInfo;
    public OrganizerDTO organizer;
    public CategoryDTO category;
    public List<CampaignImageDTO> campaignImages;
}
