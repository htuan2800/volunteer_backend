package com.volunteerBackend.service;

import java.util.List;

import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.model.Organizer;
import com.volunteerBackend.model.User;
import com.volunteerBackend.request.CampaignRequest;
import com.volunteerBackend.type.CampaignStatus;

public interface CampaignService {
    public boolean createCampaign(CampaignRequest campaign) throws Exception; 
    public List<Campaign> getAllCampaigns();
    public List<Campaign> getSearchCampaigns(String keyword);
    public List<Campaign> getCampaignByOrganizerAndStatus(Organizer organizer, CampaignStatus status);
    public boolean changeCampaignStatus(Long campaignId, CampaignStatus status);
    public List<Campaign> getFilteredCampaigns(Long  category, String status, String keywword);
    public Campaign getCampaign(Long campaignId);
    public Campaign getCampaignToUpdate(Long campaignId);
    public boolean updateCampaign(CampaignRequest campaign, Long campaignId);
    public boolean isTargetAmountReached(Long campaignId);

    public List<Campaign> getCampaignsOfUser(User user);
}
