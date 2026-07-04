package com.volunteerBackend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.volunteerBackend.DTO.UserDTO;
import com.volunteerBackend.model.User;

@Component
public class UserMapper {
    private final Cloudinary cloudinary;

    public UserMapper(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public UserDTO toDTO(User user) {
        if (user == null)
            return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setGender(user.getGender());
        if (user.getAvatar() != null) {
            if (user.getAvatar().startsWith("http")) {
                dto.setAvatar(user.getAvatar());
            } else {
                var transformation = new Transformation<>()
                        .width(800)
                        .crop("scale")
                        .quality("auto")
                        .fetchFormat("auto");
                String eagerUrl = cloudinary.url()
                        .transformation(transformation)
                        .generate(user.getAvatar());
                dto.setAvatar(eagerUrl);
            }
        } else {
            dto.setAvatar(null);
        }

        // dto.setAvatar(user.getAvatar());
        if (user.getCoverPhotoURL() != null) {
            dto.setPublicIDCoverPhoto(user.getCoverPhotoURL());
            var transformation = new Transformation<>()
                    .width(800)
                    .crop("scale")
                    .quality("auto")
                    .fetchFormat("auto");
            String eagerUrl = cloudinary.url()
                    .transformation(transformation)
                    .generate(user.getCoverPhotoURL());
            dto.setCoverPhotoURL(eagerUrl);

        }
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setIsActive(user.getIsActive());
        dto.setIsVerified(user.getIsVerified());
        dto.setIsDeleted(user.getIsDeleted());
        return dto;
    }

    public List<UserDTO> toDTOList(List<User> users) {
        if (users == null)
            return Collections.emptyList();

        return users.stream()
                .map(user -> toDTO(user))
                .collect(Collectors.toList());
    }
}