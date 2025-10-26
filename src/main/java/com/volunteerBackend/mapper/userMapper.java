package com.volunteerBackend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.UserDTO;
import com.volunteerBackend.config.FileStorageProperties;
import com.volunteerBackend.model.User;

@Component
public class UserMapper {
    private final FileStorageProperties fileStorageProperties;

    public UserMapper(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
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
                dto.setAvatar(fileStorageProperties.getBaseUrl() + user.getAvatar());
            }
        } else {
            dto.setAvatar(null);
        }

        // dto.setAvatar(user.getAvatar());
        if (user.getCoverPhotoURL() != null) {
            dto.setCoverPhotoURL(fileStorageProperties.getBaseUrl() + user.getCoverPhotoURL());
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