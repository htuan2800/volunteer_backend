package com.volunteerBackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.DTO.CampaignDTO;
import com.volunteerBackend.DTO.CampaignSummaryDTO;
import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.request.CampaignRequest;
import com.volunteerBackend.service.CampaignService;
import com.volunteerBackend.type.CampaignStatus;
import com.volunteerBackend.mapper.CampaignMapper;
import com.volunteerBackend.mapper.CampaignSummaryMapper;

@RestController
public class CampaignController {
    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignMapper campaignMapper;

    @Autowired
    private CampaignSummaryMapper campaignSummaryMapper;

    // Dành cho Admin
    @GetMapping("/api/campaigns")
    public ResponseEntity<List<CampaignDTO>> getCampaigns(
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        List<Campaign> campaigns = campaignService.getAllCampaigns();
        List<CampaignDTO> categoryDTOs = campaignMapper.toDTOListBasicNotStoryInfo(campaigns);
        return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
    }

    @GetMapping("/api/campaigns/search")
    public ResponseEntity<List<CampaignDTO>> getSearchCampaigns(
            @RequestHeader(value = "Authorization", required = false) String jwt,
            @RequestParam(required = false) String keyword
        ) 
    {
        List<Campaign> campaigns = campaignService.getSearchCampaigns(keyword);
        List<CampaignDTO> categoryDTOs = campaignMapper.toDTOListBasicNotStoryInfo(campaigns);
        return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
    }

    @PostMapping("/api/campaigns/add_campaign")
    public ResponseEntity<?> createCampaign(@RequestBody CampaignRequest campaign) throws Exception {
        boolean isSuccess = campaignService.createCampaign(campaign);
        return new ResponseEntity<>(isSuccess, HttpStatus.CREATED);
    }

    @PutMapping("/api/campaigns/change_status/{id}")
    public ResponseEntity<?> changeCampaignStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        CampaignStatus status;
        try {
            status = CampaignStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + statusStr);
        }

        boolean isSuccess = campaignService.changeCampaignStatus(id, status);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }

    // Dành cho All
    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignSummaryDTO>> getFilteredCampaigns(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        List<Campaign> campaigns = campaignService.getFilteredCampaigns(category, status, keyword);
        List<CampaignSummaryDTO> campaignDTOs = campaignSummaryMapper.toDTOListBasicNotStoryInfo(campaigns);
        return new ResponseEntity<>(campaignDTOs, HttpStatus.OK);
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<CampaignSummaryDTO> getCampaign(@PathVariable Long id) {
        Campaign campaign = campaignService.getCampaign(id);
        CampaignSummaryDTO CampaignSummaryDTO = campaignSummaryMapper.toDTOWithImage(campaign);
        return new ResponseEntity<>(CampaignSummaryDTO, HttpStatus.OK);
    }

    @GetMapping("/api/campaigns/{id}")
    public ResponseEntity<CampaignDTO> getCampaignAdmin(@PathVariable Long id) {
        Campaign campaign = campaignService.getCampaign(id);
        CampaignDTO CampaignSummaryDTO = campaignMapper.toDTOBasic(campaign);
        return new ResponseEntity<>(CampaignSummaryDTO, HttpStatus.OK);
    }

    @PutMapping("/api/campaigns/update_campaign/{id}")
    public ResponseEntity<?> updateCampaign( @PathVariable Long id, @RequestBody CampaignRequest campaign) throws Exception {
        boolean isSuccess = campaignService.updateCampaign(campaign, id);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }
}
