package com.volunteerBackend.request;

import com.volunteerBackend.type.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String fullName;
    private Gender gender;
    private String avatar;
    private String coverPhotoURL;
    private String phoneNumber;
    private String option;
}
