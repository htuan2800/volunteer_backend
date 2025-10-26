package com.volunteerBackend.service;

import java.util.List;

import com.volunteerBackend.model.CampaignImage;
import com.volunteerBackend.request.CampaignImageRequest;

public interface CampaignImageService {
    List<CampaignImage> getCampaignImages(Long campaignId);
    boolean createCampaignImages(List<CampaignImageRequest> campaignImages);
    boolean syncCampaignImages(Long campaignId, List<CampaignImageRequest> newImageDTOs);
}
