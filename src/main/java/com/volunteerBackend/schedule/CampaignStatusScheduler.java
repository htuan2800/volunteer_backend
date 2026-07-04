package com.volunteerBackend.schedule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.repository.CampaignRepository;
import com.volunteerBackend.service.IndexingService;
import com.volunteerBackend.type.CampaignStatus;

import java.time.LocalDate;
import java.util.List;
@Component
public class CampaignStatusScheduler {
    private final CampaignRepository campaignRepository;
    private final IndexingService indexingService;
    public CampaignStatusScheduler(CampaignRepository campaignRepository, IndexingService indexingService) {
        this.campaignRepository = campaignRepository;
        this.indexingService = indexingService;
    }
    @Scheduled(cron = "0 1 0 * * ?")
    public void updateEndedCampaigns() {
        System.out.println("Running scheduled task to update ended campaigns...");
        List<Campaign> campaignsToUpdate = campaignRepository
            .findActiveCampaignsPastEndDate(LocalDate.now());

        for (Campaign campaign : campaignsToUpdate) {
            campaign.setStatus(CampaignStatus.ENDED);
            indexingService.updateIndexedCampaign(campaign);
        }

        campaignRepository.saveAll(campaignsToUpdate);
        System.out.println("Finished updating " + campaignsToUpdate.size() + " campaigns.");
    }
}
