package com.volunteerBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.DTO.CampaignImageDTO;
import com.volunteerBackend.mapper.CampaignImageMapper;
import com.volunteerBackend.model.CampaignImage;
import com.volunteerBackend.request.CampaignImageRequest;
import com.volunteerBackend.service.CampaignImageService;

@RestController
public class CampaignImageController {
    @Autowired
    private CampaignImageService campaignImageService;

    @Autowired
    private CampaignImageMapper campaignImageMapper;

    @GetMapping("/api/campaigns/images/{id}")
    public ResponseEntity<List<CampaignImageDTO>> getCampaignImages(@PathVariable Long id) {
        List <CampaignImage> campaignImages = campaignImageService.getCampaignImages(id);
        return ResponseEntity.ok(campaignImageMapper.toDTOList(campaignImages));
    }


    @PutMapping("/api/campaigns/images/{id}")
    public  ResponseEntity<Boolean> updateCampaignImages(@PathVariable Long id, @RequestBody List<CampaignImageRequest> campaignImages) {
        return ResponseEntity.ok(campaignImageService.syncCampaignImages(id, campaignImages));
    }
}
