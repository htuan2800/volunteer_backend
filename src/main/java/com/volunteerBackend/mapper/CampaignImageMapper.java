package com.volunteerBackend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.volunteerBackend.DTO.CampaignImageDTO;
import com.volunteerBackend.model.CampaignImage;

@Component
public class CampaignImageMapper {

    private final Cloudinary cloudinary;

    public CampaignImageMapper(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;

    }

    public CampaignImageDTO toDTO(CampaignImage campaignImage) {
        if (campaignImage == null)
            return null;

        CampaignImageDTO dto = new CampaignImageDTO();
        dto.setId(campaignImage.getId());
        var transformation = new Transformation<>()
                .width(800)
                .crop("scale")
                .quality("auto")
                .fetchFormat("auto");
        String eagerUrl = cloudinary.url()
                .transformation(transformation)
                .generate(campaignImage.getImageUrl());
        dto.setUrl(eagerUrl);
        dto.setPublicId(campaignImage.getImageUrl());
        dto.setSortOrder(campaignImage.getSortOrder());
        return dto;
    }

    public List<CampaignImageDTO> toDTOList(List<CampaignImage> campaignImages) {
        if (campaignImages == null)
            return Collections.emptyList();

        return campaignImages.stream()
                .map(campaign -> toDTO(campaign))
                .collect(Collectors.toList());
    }
}
