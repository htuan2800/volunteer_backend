package com.volunteerBackend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.stereotype.Service;

import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.repository.CampaignRepository;
import com.volunteerBackend.type.CampaignStatus;

@Service
public class IndexingService {
    
    private VectorStore vectorStore;

    
    private CampaignRepository campaignRepository;

    public void indexSingleCampaign(Campaign campaign) {
        System.out.println("Indexing single campaign: " + campaign.getId());
        List<Document> documents = createCampaignDocument(campaign.getId());
        vectorStore.add(documents);
    }

    public void deleteIndexedCampaign(Long campaignId) {
        System.out.println("Deleting indexed campaign: " + campaignId);
        String docId1 = "campaign_" + campaignId + "_title";
        String docId2 = "campaign_" + campaignId + "_story";
        List<String> idsToDelete = List.of(docId1, docId2);
        try {
            // - Nếu có: nó xóa.
            // - Nếu không có (ví dụ lần đầu tạo): Redis tự bỏ qua, KHÔNG LỖI.
            vectorStore.delete(idsToDelete);

            System.out.println("Deleted (if existed) chunks for campaign: " + campaignId);

        } catch (Exception e) {
            // Chỉ bắt lỗi nếu có vấn đề về mạng hoặc kết nối Redis
            System.err.println("Lỗi khi cố xóa vector (có thể bỏ qua): " + e.getMessage());
        }

    }

    public void updateIndexedCampaign(Campaign campaign) {
        System.out.println("Updating indexed campaign: " + campaign.getId());
        deleteIndexedCampaign(campaign.getId());
        indexSingleCampaign(campaign);
    }

    private List<Document> createCampaignDocument(Long id) {
        Campaign campaign = campaignRepository.findByIdWithDetails(id);
        List<Document> documents = new ArrayList<>();
        String statusText = "";
        if (campaign.getStatus() == CampaignStatus.ENDED) {
            statusText = "TRẠNG THÁI: Đã kết thúc.\n";
        } else if (campaign.getStatus() == CampaignStatus.IN_PROGRESS) {
            statusText = "TRẠNG THÁI: Đang kêu gọi quyên góp.\n";
        } else if (campaign.getStatus() == CampaignStatus.PAUSED) {
            statusText = "TRẠNG THÁI: Đã tạm dừng.\n";
        } else if (campaign.getStatus() == CampaignStatus.TARGET_REACHED) {
            statusText = "TRẠNG THÁI: Đã đạt mục tiêu (kết thúc thành công).\n";
        }
        String titleContent = statusText +
                "Tên chiến dịch: " + campaign.getTitle() + "\n" +
                "Mục tiêu: " + campaign.getTargetAmount() + "\n" +
                "Danh mục: " + campaign.getCategory().getName() + "\n" +
                "Tổ chức: " + campaign.getOrganizer().getName();

        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("source", "db_campaign_title");
        metadata1.put("campaign_id", campaign.getId());
        metadata1.put("campaign_title", campaign.getTitle());

        String docId1 = "campaign_" + campaign.getId() + "_title";
        documents.add(new Document(docId1, titleContent, metadata1));

        String storyContent = statusText +
                "Mô tả chi tiết chiến dịch " + campaign.getTitle() + ": \n" +
                campaign.getStoryInfo();

        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("source", "db_campaign_story");
        metadata2.put("campaign_id", campaign.getId());
        metadata2.put("campaign_title", campaign.getTitle());

        String docId2 = "campaign_" + campaign.getId() + "_story";
        documents.add(new Document(docId2, storyContent, metadata2));

        return documents;
    }
}
