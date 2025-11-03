package com.volunteerBackend.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.model.CampaignImage;
import com.volunteerBackend.repository.CampaignImageRepository;
import com.volunteerBackend.repository.CampaignRepository;
import com.volunteerBackend.request.CampaignImageRequest;

@Service
public class CampaignImageServiceImp implements CampaignImageService {

    @Autowired
    private CampaignImageRepository campaignImageRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Override
    public List<CampaignImage> getCampaignImages(Long campaignId) {
        return campaignImageRepository.findByCampaignId(campaignId);
    }

    @Override
    public boolean createCampaignImages(List<CampaignImageRequest> campaignImages) {
        return false;
    }

    @Override
    public boolean syncCampaignImages(Long campaignId, List<CampaignImageRequest> newImageDTOs) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Bước 2 & 3: Lấy danh sách cũ và chuyển thành Map
        Map<Long, CampaignImage> oldImageMap = campaignImageRepository.findByCampaignId(campaignId)
                .stream()
                .collect(Collectors.toMap(CampaignImage::getId, Function.identity()));

        // Bước 4: Lặp qua danh sách mới
        for (CampaignImageRequest dto : newImageDTOs) {
            if (dto.getSortOrder() == 0) {
                campaign.setFeaturedImage(dto.getUrl());
                campaignRepository.save(campaign);
            }
            if (dto.getId() != null) { // Là ảnh cũ, cần kiểm tra UPDATE
                CampaignImage existingImage = oldImageMap.get(dto.getId());
                if (existingImage != null) {
                    // Cập nhật sortOrder nếu có thay đổi
                    if (existingImage.getSortOrder() != dto.getSortOrder()) {

                        existingImage.setSortOrder(dto.getSortOrder());
                        campaignImageRepository.save(existingImage);
                    }
                    // Đánh dấu đã xử lý bằng cách xóa khỏi map
                    oldImageMap.remove(dto.getId());
                }
            } else { // Là ảnh mới, cần INSERT
                CampaignImage newImage = new CampaignImage();
                newImage.setCampaign(campaign);
                newImage.setImageUrl(dto.getUrl());
                newImage.setSortOrder(dto.getSortOrder());
                campaignImageRepository.save(newImage);
            }
        }

        // Bước 5: Xóa những ảnh còn sót lại trong map (ảnh đã bị xóa ở client)
        if (!oldImageMap.isEmpty()) {
            campaignImageRepository.deleteAll(oldImageMap.values());
        }

        return true;
    }
}
