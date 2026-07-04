package com.volunteerBackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignImageDTO {
    public Long id;
    public String publicId;
    public String url;
    public Integer sortOrder;
}
