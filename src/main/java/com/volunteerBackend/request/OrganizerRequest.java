package com.volunteerBackend.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerRequest {
    private String name;
    private String description;
    private String logoUrl;
    private String slug;
    private String hotline;
    private String email;
    private String option;
}
