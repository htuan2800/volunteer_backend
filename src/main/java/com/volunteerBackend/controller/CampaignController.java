package com.volunteerBackend.controller;

import java.util.List;
import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.DTO.CampaignDTO;
import com.volunteerBackend.DTO.CampaignSummaryDTO;
import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.model.Organizer;
import com.volunteerBackend.request.CampaignRequest;
import com.volunteerBackend.service.CampaignService;
import com.volunteerBackend.service.OrganizerService;
import com.volunteerBackend.type.CampaignStatus;
import com.volunteerBackend.mapper.CampaignMapper;
import com.volunteerBackend.mapper.CampaignSummaryMapper;

@RestController
@RequestMapping("/api")
public class CampaignController {
    
    private CampaignService campaignService;

    
    private OrganizerService organizerService;

    
    private CampaignMapper campaignMapper;

    
    private CampaignSummaryMapper campaignSummaryMapper;

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
    public ResponseEntity<CampaignSummaryDTO> getCampaignsByUser(@PathVariable Long id) {
        Campaign campaign = campaignService.getCampaign(id);
        CampaignSummaryDTO CampaignSummaryDTO = campaignSummaryMapper.toDTOWithImage(campaign);
        return new ResponseEntity<>(CampaignSummaryDTO, HttpStatus.OK);
    }

    @GetMapping("/campaigns/organizer/{id}")
    public ResponseEntity<List<CampaignSummaryDTO>> getCampaignByOrganizerIDAndStatus(@PathVariable Integer id, @RequestParam CampaignStatus status) {
        Organizer organizer = organizerService.findOrganizerById(id);
        List<Campaign> campaigns = campaignService.getCampaignByOrganizerAndStatus(organizer,status);
        List<CampaignSummaryDTO> campaignDTOs = campaignSummaryMapper.toDTOListBasicNotStoryInfo(campaigns);
        return new ResponseEntity<>(campaignDTOs, HttpStatus.OK);
    }

    // Dành cho Admin
    @GetMapping("/admin/campaigns")
    public ResponseEntity<List<CampaignDTO>> getCampaignsByAdmin() {
        List<Campaign> campaigns = campaignService.getAllCampaigns();
        List<CampaignDTO> categoryDTOs = campaignMapper.toDTOListBasicNotStoryInfo(campaigns);
        return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
    }

    @GetMapping("/admin/campaigns/search")
    public ResponseEntity<List<CampaignDTO>> getSearchCampaigns(
            @RequestParam(required = false) String keyword
        ) 
    {
        List<Campaign> campaigns = campaignService.getSearchCampaigns(keyword);
        List<CampaignDTO> categoryDTOs = campaignMapper.toDTOListBasicNotStoryInfo(campaigns);
        return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
    }

    @PostMapping("/admin/campaigns/add_campaign")
    public ResponseEntity<?> createCampaign(@RequestBody CampaignRequest campaign) throws Exception {
        boolean isSuccess = campaignService.createCampaign(campaign);
        return new ResponseEntity<>(isSuccess, HttpStatus.CREATED);
    }

    @PutMapping("/admin/campaigns/change_status/{id}")
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

    @GetMapping("/admin/campaigns/{id}")
    public ResponseEntity<CampaignDTO> getCampaignAdmin(@PathVariable Long id) {
        Campaign campaign = campaignService.getCampaign(id);
        CampaignDTO CampaignSummaryDTO = campaignMapper.toDTOBasic(campaign);
        return new ResponseEntity<>(CampaignSummaryDTO, HttpStatus.OK);
    }

    @PutMapping("/admin/campaigns/update_campaign/{id}")
    public ResponseEntity<?> updateCampaign( @PathVariable Long id, @RequestBody CampaignRequest campaign) throws Exception {
        boolean isSuccess = campaignService.updateCampaign(campaign, id);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }
}
