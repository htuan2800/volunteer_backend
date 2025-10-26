package com.volunteerBackend.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.CampaignDTO;
import com.volunteerBackend.DTO.CampaignImageDTO;
import com.volunteerBackend.config.FileStorageProperties;
import com.volunteerBackend.model.Campaign;

@Component
public class CampaignMapper {
    private final OrganizerMapper organizerMapper;
    private final CategoryMapper categoryMapper;
    private final FileStorageProperties fileStorageProperties;
    public CampaignMapper(OrganizerMapper organizerMapper, CategoryMapper categoryMapper,
            CampaignImageMapper CampaignImageMapper, FileStorageProperties fileStorageProperties) {
        this.organizerMapper = organizerMapper;
        this.categoryMapper = categoryMapper;
        this.fileStorageProperties = fileStorageProperties;
    }

    public CampaignDTO toDTOBasic(Campaign campaign) {
        CampaignDTO dto = new CampaignDTO();
        dto.setCampaignId(campaign.getId());
        dto.setTitle(campaign.getTitle());
        dto.setTargetAmount(campaign.getTargetAmount());
        dto.setFeaturedImage(fileStorageProperties.getBaseUrl() + campaign.getFeaturedImage());
        dto.setStatus(campaign.getStatus());
        dto.setStartDate(campaign.getStartDate());
        dto.setEndDate(campaign.getEndDate());
        dto.setStoryInfo(campaign.getStoryInfo());
        dto.setOrganizer(organizerMapper.toDTO(campaign.getOrganizer()));
        dto.setCategory(categoryMapper.toDTO(campaign.getCategory()));
        return dto;
    }

    public CampaignDTO toDTOBasicNotStoryInfo(Campaign campaign) {
        CampaignDTO dto = new CampaignDTO();
        dto.setCampaignId(campaign.getId());
        dto.setTitle(campaign.getTitle());
        dto.setTargetAmount(campaign.getTargetAmount());
        dto.setFeaturedImage(fileStorageProperties.getBaseUrl() + campaign.getFeaturedImage());
        dto.setStatus(campaign.getStatus());
        dto.setStartDate(campaign.getStartDate());
        dto.setEndDate(campaign.getEndDate());
        dto.setOrganizer(organizerMapper.toDTO(campaign.getOrganizer()));
        dto.setCategory(categoryMapper.toDTO(campaign.getCategory()));
        return dto;
    }


    // toDTO đầy đủ (có image)
    public CampaignDTO toDTOWithImage(Campaign campaign) {
        CampaignDTO dto = toDTOBasic(campaign);

        // Map List<CampaignImage> sang List<CampaignImageDTO>
        if (campaign.getImages() != null) {
            List<CampaignImageDTO> imageDTOs = campaign.getImages().stream()
                    .map(campaignImage -> new CampaignImageDTO(
                            campaignImage.getId(),
                            campaignImage.getImageUrl(),
                            campaignImage.getSortOrder()))
                    .toList();
            dto.setCampaignImages(imageDTOs);
        } else {
            dto.setCampaignImages(Collections.emptyList());
        }

        return dto;
    }

    // --- List DTO ---
    public List<CampaignDTO> toDTOListBasic(List<Campaign> campaigns) {
        return campaigns.stream()
                .map(this::toDTOBasic)
                .toList();
    }

    public List<CampaignDTO> toDTOListBasicNotStoryInfo(List<Campaign> campaigns) {
        return campaigns.stream()
                .map(this::toDTOBasicNotStoryInfo)
                .toList();
    }

    public List<CampaignDTO> toDTOListWithImage(List<Campaign> campaigns) {
        return campaigns.stream()
                .map(this::toDTOWithImage)
                .toList();
    }
}
