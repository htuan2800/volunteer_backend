package com.volunteerBackend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.repository.CampaignRepository;
import com.volunteerBackend.type.CampaignStatus;

import redis.clients.jedis.exceptions.JedisDataException;

@Service
public class SchemaVectorService {

    private final VectorStore vectorStore;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private CampaignRepository campaignRepository;

    @Value("${spring.ai.vectorstore.redis.index-name}")
    private String indexName;

    public SchemaVectorService(VectorStore vectorStore, RedisTemplate<String, Object> redisTemplate) {
        this.vectorStore = vectorStore;
        this.redisTemplate = redisTemplate;
        System.out.println("!!! [DEBUG] VectorStore Bean đã được tiêm là: " + vectorStore.getClass().getName());
    }

    /**
     * Index database schema vào Redis Vector Store
     * Gọi 1 lần khi khởi động hoặc khi schema thay đổi
     */
    // @PostConstruct
    public void indexAllDataSources() {
        try {
            System.out.println("Starting to index all data sources...");
            List<Document> allDocuments = new ArrayList<>();

            // 2. Tải và chia nhỏ các file text
            allDocuments.addAll(loadAndSplitTextFile("src/main/resources/policy.txt", "policy"));
            allDocuments.addAll(loadAndSplitTextFile("src/main/resources/term.txt", "term"));

            // 3. Tải dữ liệu từ Database
            allDocuments.addAll(loadCampaignsFromDatabase());

            if (allDocuments.isEmpty()) {
                System.out.println("Không tìm thấy document nào để index.");
                return;
            }
            System.out.println("Indexing " + allDocuments.size() + " total documents into Redis...");
            vectorStore.add(allDocuments); // <-- Nạp toàn bộ
            System.out.println("Done indexing all data sources.");

        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng khi indexing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tải 1 file text, chia nhỏ theo các đoạn văn (dòng trống)
     */
    private List<Document> loadAndSplitTextFile(String filePath, String sourceType) throws IOException {
        System.out.println("Loading file: " + filePath);
        List<Document> documents = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(filePath)));

        // CHIA MỚI: Chia theo các dòng trống (cách nhau 2 dấu \n)
        String[] chunks = content.split("(\\r?\\n){2,}"); // Regex: chia theo 1 hoặc nhiều dòng trống

        for (String chunk : chunks) {
            String trimmedChunk = chunk.trim();
            if (trimmedChunk.isEmpty())
                continue;

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", sourceType); // Quan trọng: biết nó từ file nào
            metadata.put("file_path", filePath);
            documents.add(new Document(trimmedChunk, metadata));
        }
        System.out.println("Loaded " + documents.size() + " chunks from " + filePath);
        return documents;
    }

    /**
     * Tải dữ liệu các chiến dịch từ CSDL
     */
    private List<Document> loadCampaignsFromDatabase() {
        System.out.println("Loading campaigns from database...");
        List<Document> documents = new ArrayList<>();
        List<Campaign> campaigns = campaignRepository.findAllWithDetails(); // Lấy tất cả

        for (Campaign campaign : campaigns) {
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

        }
        System.out.println("Loaded " + documents.size() + " campaigns from database.");
        return documents;
    }

    /**
     * Tìm kiếm schema liên quan dựa trên user query
     */
    public String findRelevantSchema(String userQuery, int topK) {
        // Tìm các documents liên quan nhất
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuery)
                        .topK(topK)
                        .similarityThreshold(0.5)
                        .build());

        // Ghép các schema chunks lại
        StringBuilder relevantSchema = new StringBuilder();
        for (Document doc : relevantDocs) {
            relevantSchema.append(doc.getText()).append("\n\n");
        }
        return relevantSchema.toString();
    }

    /**
     * Xóa và re-index schema (dùng khi cần refresh)
     */
    public void reindexSchemaOnStartup() {
        System.out.println("--- [SCHEMA RE-INDEXING] Bắt đầu quá trình re-index ---");

        try {
            // Bước 1: Gửi lệnh gốc để XÓA index cũ (nếu có)
            // 'DD' = Drop Documents (xóa cả các document liên quan)
            System.out.println("Đang thử xóa index cũ (nếu tồn tại): " + indexName);

            Objects.requireNonNull(redisTemplate.getConnectionFactory())
                    .getConnection()
                    .execute(
                            "FT.DROPINDEX",
                            indexName.getBytes(StandardCharsets.UTF_8),
                            "DD".getBytes(StandardCharsets.UTF_8));

            System.out.println("Đã xóa index cũ thành công.");

        } catch (JedisDataException e) {
            // Coi như thành công (vì không có gì để xóa).
            if (e.getMessage().contains("Unknown Index name")) {
                System.out.println("Index chưa tồn tại, không cần xóa (bình thường trong lần chạy đầu tiên).");
            } else {
                // Nếu là lỗi khác thì mới in ra
                System.err.println("Lỗi khi xóa index (nhưng không phải 'Unknown Index'): " + e.getMessage());
            }
        } catch (Exception e) {
            // Bắt các lỗi chung khác
            System.err.println("Lỗi bất ngờ khi xóa index: " + e.getMessage());
        }

        // Bước 2: Gọi hàm indexDatabaseSchema của bạn để nạp dữ liệu mới
        System.out.println("Đang nạp (load) schema mới...");
        indexAllDataSources();

        System.out.println("--- [SCHEMA RE-INDEXING] Hoàn thành ---");
    }

}
