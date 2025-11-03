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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.repository.CampaignRepository;

import jakarta.annotation.PostConstruct;
import redis.clients.jedis.exceptions.JedisDataException;

@Service
public class SchemaVectorService {

    private final VectorStore vectorStore;
    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
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
            if (trimmedChunk.isEmpty()) continue;

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
        List<Campaign> campaigns = campaignRepository.findAll(); // Lấy tất cả

        for (Campaign campaign : campaigns) {
            // Ghép các trường text ý nghĩa lại
            String content = "Tên chiến dịch: " + campaign.getTitle() + "\n" +
                             "Mô tả: " + campaign.getStoryInfo() + "\n" +
                             "Mục tiêu: " + campaign.getTargetAmount();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "database_campaign");
            metadata.put("campaign_id", campaign.getId()); // Cực kỳ quan trọng
            metadata.put("campaign_title", campaign.getTitle());

            documents.add(new Document(content, metadata));
        }
        System.out.println("Loaded " + documents.size() + " campaigns from database.");
        return documents;
    }

    // private String generateTableDescription(String tableName, String schema) {
    //     // Đây là ví dụ đơn giản, bạn nên làm chi tiết hơn
    //     if ("categories".equalsIgnoreCase(tableName)) {
    //         return "Bảng này chứa thông tin về các danh mục chiến dịch từ thiện.";
    //     }
    //     if ("campaigns".equalsIgnoreCase(tableName)) {
    //         return "Bảng này chứa thông tin chính về các chiến dịch từ thiện";
    //     }
    //     if ("campaign_images".equalsIgnoreCase(tableName)) {
    //         return "Bảng này chứa thông tin hình ảnh liên quan đến các chiến dịch từ thiện.";
    //     }
    //     if ("donations".equalsIgnoreCase(tableName)) {
    //         return "Bảng này lưu lại lịch sử các lần quyên góp (donations) cho mỗi chiến dịch";
    //     }
    //     if ("dashboard_statistics".equalsIgnoreCase(tableName)) {
    //         return "Bảng này dùng để lưu trữ các số liệu thống kê tổng hợp cho dashboard.";
    //     }
    //     if ("users".equalsIgnoreCase(tableName)) {
    //         return "Bảng này chứa thông tin người dùng đăng ký trên hệ thống.";
    //     }
    //     if ("organizers".equalsIgnoreCase(tableName)) {
    //         return "Bảng này chứa thông tin các tổ chức trên hệ thống.";
    //     }
    //     if ("messages".equalsIgnoreCase(tableName)) {
    //         return "Bảng này chứa các tin nhắn giữa người dùng và admin hoặc admin và AI.";
    //     }
    //     if ("chat".equalsIgnoreCase(tableName)) {
    //         return "Bảng này chứa các phiên chat giữa ADMIN và AI hoặc người dùng và ADMIN.";
    //     }
    //     if ("user_providers".equalsIgnoreCase(tableName)) {
    //         return "Bảng này chứa các kiểu đăng nhập của người dùng (Google, Facebook, v.v.).";
    //     }
    //     return "Một bảng trong cơ sở dữ liệu.";
    // }
    /**
     * Tìm kiếm schema liên quan dựa trên user query
     */
    public String findRelevantSchema(String userQuery, int topK) {
        // Tìm các documents liên quan nhất
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuery)
                        .topK(topK)
                        .build());

        // Ghép các schema chunks lại
        StringBuilder relevantSchema = new StringBuilder();
        for (Document doc : relevantDocs) {
            relevantSchema.append(doc.getText()).append("\n\n");
        }
        return relevantSchema.toString();
    }

    /**
     * Trích xuất tên table từ schema
     */
    private String extractTableName(String tableSchema) {
        Pattern pattern = Pattern.compile("CREATE TABLE\\s+`?(\\w+)`?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tableSchema);
        return matcher.find() ? matcher.group(1) : "unknown";
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
