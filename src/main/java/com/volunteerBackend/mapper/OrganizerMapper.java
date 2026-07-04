package com.volunteerBackend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.volunteerBackend.DTO.OrganizerDTO;
import com.volunteerBackend.model.Organizer;


@Component
public class OrganizerMapper {

    private final Cloudinary cloudinary;


    public OrganizerMapper(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    public OrganizerDTO toDTO(Organizer organizer) {
        if (organizer == null)
            return null;

        OrganizerDTO dto = new OrganizerDTO();
        dto.setOrganizerId(organizer.getOrganizerId());
        dto.setName(organizer.getName());
        dto.setSlug(organizer.getSlug());
        dto.setDescription(organizer.getDescription());
        var transformation = new Transformation<>()
                    .crop("scale")
                    .width(200)
                    .height(200)
                    .fetchFormat("auto")
                    .quality("auto");
        String eagerUrl = cloudinary.url()
                    .transformation(transformation)
                    .generate(organizer.getLogoUrl());
        dto.setLogoUrl(eagerUrl);
        dto.setHotline(organizer.getHotline());
        dto.setEmail(organizer.getEmail());
        dto.setActive(organizer.getIsActive());
        dto.setDeleted(organizer.getIsDeleted());
        return dto;
    }

    public List<OrganizerDTO> toDTOList(List<Organizer> organizers) {
        if (organizers == null)
            return Collections.emptyList();

        return organizers.stream()
                .map(organizer -> toDTO(organizer))
                .collect(Collectors.toList());
    }
}
