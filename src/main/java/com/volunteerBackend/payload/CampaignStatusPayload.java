package com.volunteerBackend.payload;

import com.volunteerBackend.type.CampaignStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CampaignStatusPayload {
    private Long campaignId;
    private String title;
    private CampaignStatus status;
}
