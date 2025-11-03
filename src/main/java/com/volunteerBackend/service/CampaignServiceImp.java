package com.volunteerBackend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.model.CampaignImage;
import com.volunteerBackend.model.Category;
import com.volunteerBackend.model.Organizer;
import com.volunteerBackend.model.User;
import com.volunteerBackend.repository.CampaignImageRepository;
import com.volunteerBackend.repository.CampaignRepository;
import com.volunteerBackend.repository.CategoryRepository;
import com.volunteerBackend.repository.DonationRepository;
import com.volunteerBackend.repository.OrganizerRepository;
import com.volunteerBackend.request.CampaignRequest;
import com.volunteerBackend.type.CampaignStatus;
import com.volunteerBackend.type.PaymentStatus;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CampaignServiceImp implements CampaignService {
    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private CampaignImageRepository campaignImageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private DashboardStatisticsService dashboardStatisticsService;

    @Override
    public boolean createCampaign(CampaignRequest request) {
        if (campaignRepository.existsByTitle(request.getTitle())) {
            throw new IllegalArgumentException("Campaign already exists");
        }

        Organizer organizer = organizerRepository.findById(request.getOrganizer())
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));

        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Campaign campaign = new Campaign();
        campaign.setTitle(request.getTitle());
        campaign.setTargetAmount(request.getTargetAmount());
        campaign.setCategory(category);
        campaign.setOrganizer(organizer);
        campaign.setFeaturedImage(request.getFeaturedImage());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setStoryInfo(request.getStoryInfo());
        campaignRepository.save(campaign);

        CampaignImage campaignImage = new CampaignImage();
        campaignImage.setCampaign(campaign);
        campaignImage.setImageUrl(request.getFeaturedImage());
        campaignImage.setSortOrder(0);
        campaignImageRepository.save(campaignImage);

        dashboardStatisticsService.updateTotalCampaigns();


        return true;
    }

    @Override
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    @Override
    public List<Campaign> getSearchCampaigns(String keyword) {
        return campaignRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    public List<Campaign> getCampaignByOrganizerAndStatus(Organizer organizer, CampaignStatus status) {
        return campaignRepository.findByOrganizerAndStatus(organizer, status);
    }

    @Override
    public List<Campaign> getFilteredCampaigns(Long categoryID, String status, String keyword) {
        CampaignStatus campaignStatus = null;
        if (status != null && !"null".equals(status)) {
            campaignStatus = CampaignStatus.valueOf(status);
        }
        if(categoryID == null) return campaignRepository.findByStatusOrTitle(campaignStatus, keyword);
        Category category = categoryRepository.findById(categoryID).orElse(null);
        return campaignRepository.findByCategoryAndStatusOrTitle(category, campaignStatus, keyword);
    }

    @Override
    public boolean isTargetAmountReached(Long campaignId) {
        BigDecimal donateAmount = campaignRepository.sumCompletedDonationsByCampaign(campaignId, PaymentStatus.COMPLETED);
        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        return donateAmount.compareTo(campaign.getTargetAmount()) >= 0;
    }

    @Override
    public boolean changeCampaignStatus(Long campaignId, CampaignStatus status) {
        Optional<Campaign> campaignOptional = campaignRepository.findById(campaignId);
        if (campaignOptional.isPresent()) {
            Campaign campaign = campaignOptional.get();
            campaign.setStatus(status);
            campaignRepository.save(campaign);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Campaign getCampaign(Long campaignId) {
        return campaignRepository.findById(campaignId).orElse(null);
    }


    @Override
    public Campaign getCampaignToUpdate(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if(campaign.getStatus() == CampaignStatus.IN_PROGRESS) return campaign;
        return  null;
    }

    @Override
    public boolean updateCampaign(CampaignRequest campaign, Long campaignId) {
        Campaign campaignToUpdate = campaignRepository.findById(campaignId).orElse(null);
        if (campaignToUpdate == null) {
            return false;
        }
        campaignToUpdate.setTitle(campaign.getTitle());
        campaignToUpdate.setTargetAmount(campaign.getTargetAmount());
        campaignToUpdate.setCategory(categoryRepository.findById(campaign.getCategory()).orElse(null));
        campaignToUpdate.setOrganizer(organizerRepository.findById(campaign.getOrganizer()).orElse(null));
        campaignToUpdate.setStartDate(campaign.getStartDate());
        campaignToUpdate.setEndDate(campaign.getEndDate());
        campaignToUpdate.setStoryInfo(campaign.getStoryInfo());
        campaignRepository.save(campaignToUpdate);
        return true;
    }

    @Override
    public List<Campaign> getCampaignsOfUser(User user) {
        return donationRepository.findDistinctCampaignsByDonorAndPaymentStatus(user, PaymentStatus.COMPLETED);
    }
}
