package com.volunteerBackend.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignImageRequest {
    private Long id;
    private String url;
    private Integer sortOrder;
}
