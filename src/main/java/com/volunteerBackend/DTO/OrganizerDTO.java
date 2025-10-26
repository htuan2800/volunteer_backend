package com.volunteerBackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizerDTO {
    public Integer organizerId;
    public String name;
    public String description;
    public String logoUrl;
    public String slug;
    public String hotline;
    public String email;
    public boolean isActive;
    public boolean isDeleted;
}
