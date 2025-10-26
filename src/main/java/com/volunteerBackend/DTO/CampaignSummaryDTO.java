package com.volunteerBackend.DTO;

import java.math.BigDecimal;
import java.util.List;

import com.volunteerBackend.type.CampaignStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignSummaryDTO {
    public Long campaignId;
    public String title;
    public BigDecimal targetAmount;
    public BigDecimal currentAmount;
    public Integer percentage;
    public String storyInfo;
    public String featuredImage;
    public Integer SupportCount;
    public Integer dayLeft;
    public CampaignStatus status;
    public OrganizerDTO organizer; 
    public List<CampaignImageDTO> campaignImages;
}
