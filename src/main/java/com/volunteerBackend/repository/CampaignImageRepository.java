package com.volunteerBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.volunteerBackend.model.CampaignImage;

@Repository
public interface CampaignImageRepository extends JpaRepository<CampaignImage, Long> {
    List <CampaignImage> findByCampaignId(Long campaignId);
}
