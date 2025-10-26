package com.volunteerBackend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.CampaignImageDTO;
import com.volunteerBackend.config.FileStorageProperties;
import com.volunteerBackend.model.CampaignImage;

@Component
public class CampaignImageMapper {
    private final FileStorageProperties fileStorageProperties;

    public CampaignImageMapper(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }
    public CampaignImageDTO toDTO(CampaignImage categoryImage) {
        if (categoryImage == null)
            return null;

        CampaignImageDTO dto = new CampaignImageDTO();
        dto.setId(categoryImage.getId());
        dto.setUrl( fileStorageProperties.getBaseUrl() + categoryImage.getImageUrl());
        dto.setSortOrder(categoryImage.getSortOrder());
        return dto;
    }

    public List<CampaignImageDTO> toDTOList(List<CampaignImage> categorieImages) {
        if (categorieImages == null)
            return Collections.emptyList();

        return categorieImages.stream()
                .map(category -> toDTO(category))
                .collect(Collectors.toList());
    }
}
