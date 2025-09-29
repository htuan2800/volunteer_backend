package com.volunteerBackend.DTO;
import java.time.LocalDateTime;

import com.volunteerBackend.type.Gender;
import com.volunteerBackend.type.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class userDTO {
    private Integer id;
    private String fullName;
    private String email;
    private Gender gender;
    private String avatar;
    private String coverPhotoURL;
    private UserRole role;
    private LocalDateTime createdAt;
    private Boolean isVerified;    
}
