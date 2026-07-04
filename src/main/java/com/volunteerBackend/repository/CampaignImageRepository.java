package com.volunteerBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.volunteerBackend.model.CampaignImage;

public interface CampaignImageRepository extends JpaRepository<CampaignImage, Long> {
    List <CampaignImage> findByCampaignId(Long campaignId);
}
