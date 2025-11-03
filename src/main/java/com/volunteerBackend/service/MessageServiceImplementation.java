package com.volunteerBackend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.volunteerBackend.DTO.MessageDTO;
import com.volunteerBackend.mapper.MessageMapper;
import com.volunteerBackend.model.Chat;
import com.volunteerBackend.model.Message;
import com.volunteerBackend.model.User;
import com.volunteerBackend.repository.ChatRepository;
import com.volunteerBackend.repository.MessageRepository;
import com.volunteerBackend.repository.UserRepository;
import com.volunteerBackend.request.MessageRequest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Flux;

@Service
public class MessageServiceImplementation implements MessageService {
    private static final String SCHEMA_FILE = "src/main/resources/db_schema.txt";

    private final JdbcTemplate jdbcTemplate;
    private final ChatClient chatClient;
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;
    @PersistenceContext
    private final EntityManager entityManager;

    private final SchemaVectorService schemaVectorService;

    public MessageServiceImplementation(JdbcTemplate jdbcTemplate,
            ChatClient.Builder chatClient,
            MessageRepository messageRepository,
            ChatRepository chatRepository,
            UserRepository userRepository,
            MessageMapper messageMapper,
            SimpMessagingTemplate messagingTemplate,
            EntityManager entityManager,
            SchemaVectorService schemaVectorService) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatClient = chatClient.build();
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.messageMapper = messageMapper;
        this.messagingTemplate = messagingTemplate;
        this.entityManager = entityManager;
        this.schemaVectorService = schemaVectorService;
    }

    @Transactional
    @Override
    public MessageDTO createMessage(MessageRequest req) throws Exception {
        Chat chat = chatRepository.findById(req.getId())
                .orElseThrow(() -> new Exception("Chat not found"));

        User user = userRepository.findById(req.getSenderId())
                .orElseThrow(() -> new Exception("User not found"));

        Message message = new Message();
        message.setChat(chat);
        message.setContent(req.getText());
        message.setUser(user);
        message.setTimestamp(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);
        chatRepository.save(chat);

        return messageMapper.toDTOWithTempId(savedMessage, req.getTempId());
    }

    @Override
    @Transactional
    public void processUserMessage(MessageRequest req) {
        String sessionId = req.getSessionId();

        try {
            // 1. Lấy chat session
            Chat chat = entityManager
                    .createQuery("SELECT c FROM Chat c WHERE c.sessionId = :sessionId", Chat.class)
                    .setParameter("sessionId", sessionId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (chat == null) {
                throw new Exception("Chat session not found");
            }

            // 2. Lưu tin nhắn của user
            MessageDTO userMessage = createMessage(req);
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId, Map.of(
                        "type", "USER_MESSAGE",
                        "message", userMessage));

            // 3. Xử lý AI query và gửi response
            if (req.getType().equals("AI_WITH_ADMIN")) {
                processAndRespondofAIWithADMIN(req.getText(), sessionId, chat);
            } else if (req.getType().equals("USER_WITH_AI")) {
                processAndRespondOfAIWithUSER(req.getText(), sessionId, chat);
            }

        } catch (Exception e) {
            System.err.println("Lỗi xử lý tin nhắn: " + e.getMessage());
            e.printStackTrace();
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + sessionId,
                    "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public MessageDTO saveAiMessage(Chat chat, String content, MessageType messageType) {
        Message aiMessage = new Message();
        aiMessage.setContent(content);
        aiMessage.setChat(chat);
        aiMessage.setTimestamp(LocalDateTime.now());
        aiMessage.setMessageType(messageType);

        messageRepository.save(aiMessage);
        return messageMapper.toDTO(aiMessage);
    }

    @Override
    public List<MessageDTO> findChatsMessages(Integer chatId, User user) throws Exception {
        List<Message> messages = messageRepository.findByChatId(chatId);
        return messageMapper.toDTOList(messages);
    }

    // Service AI
    public void processAndRespondofAIWithADMIN(String userQuery, String sessionId, Chat chat) throws IOException {
        try {
            String schema = loadDatabaseSchema();
            String generatedSql = generateSqlQuery(userQuery, schema);

            if ("NOT_DB_QUERY".equals(generatedSql)) {
                handleGeneralChat(userQuery, sessionId, chat);
            } else {
                handleDatabaseQuery(generatedSql, userQuery, sessionId, chat);
            }
        } catch (Exception e) {
            handleError(sessionId, chat, e);
        }
    }

     public void processAndRespondOfAIWithUSER(String userQuery, String sessionId, Chat chat) throws IOException {
        try {
            String relevantSchema = schemaVectorService.findRelevantSchema(userQuery, 3);
            generateResponseFromDataOfAIWithUser(relevantSchema, userQuery, sessionId, chat);

        } catch (Exception e) {
            handleError(sessionId, chat, e);
        }
    }

    private String loadDatabaseSchema() throws IOException {
        return new String(Files.readAllBytes(Paths.get(SCHEMA_FILE)));
    }

    private String generateSqlQuery(String userQuery, String schema) {
        String requiredString = """
                    Bạn là một chuyên gia tạo SQL MySQL.
                    Dựa trên Schema CSDL dưới đây:
                    ---
                    {db_schema}
                    ---
                    Hãy viết một câu lệnh MySQL SELECT thô duy nhất cho yêu cầu của người dùng.
                    Yêu cầu người dùng: {user_query}

                    QUAN TRỌNG:
                    1. Chỉ trả lời bằng câu lệnh SELECT. Bắt đầu bằng "SELECT".
                    2. Nếu yêu cầu của người dùng không thể trả lời bằng cách truy vấn schema này (ví dụ: hỏi về thời tiết, gợi ý ý tưởng...), hãy trả về chuỗi "NOT_DB_QUERY".
                """;
        PromptTemplate sqlPromptTemplate = new PromptTemplate(requiredString);

        String sqlPrompt = sqlPromptTemplate.render(Map.of(
                "db_schema", schema,
                "user_query", userQuery));

        ChatResponse response = chatClient.prompt(sqlPrompt).call().chatResponse();
        String fullGeneratedSql = response.getResult().getOutput().getText();

        return cleanAIResponse(fullGeneratedSql);
    }



    /**
     * Xử lý chat thông thường (không liên quan DB)
     */
    private void handleGeneralChat(String userQuery, String sessionId, Chat chat) {
        PromptTemplate chatPromptTemplate = new PromptTemplate(
                "Bạn là trợ lý AI hữu ích cho trang web từ thiện.\n" +
                        "Trả lời câu hỏi: {user_query} bằng tiếng Việt, thân thiện.");

        String chatPrompt = chatPromptTemplate.render(Map.of("user_query", userQuery));
        Flux<ChatResponse> chatResponseFlux = chatClient.prompt(chatPrompt).stream().chatResponse();

        streamResponse(chatResponseFlux, sessionId, chat);
    }

    /**
     * Xử lý database query
     */
    private void handleDatabaseQuery(String generatedSql, String userQuery, String sessionId, Chat chat) {
        if (!generatedSql.toUpperCase().startsWith("SELECT")) {
            sendErrorMessage(sessionId, chat, "Truy vấn không hợp lệ: " + generatedSql);
            return;
        }

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(generatedSql);

            if (results.isEmpty()) {
                sendErrorMessage(sessionId, chat, "Không tìm thấy dữ liệu cho câu hỏi của bạn.");
                return;
            }

            generateResponseFromData(results, userQuery, sessionId, chat);

        } catch (Exception e) {
            sendErrorMessage(sessionId, chat, "Lỗi khi thực hiện truy vấn: " + e.getMessage());
        }
    }

    /**
     * Tạo response từ data
     */
    private void generateResponseFromData(List<Map<String, Object>> results, String userQuery,
            String sessionId, Chat chat) {
        String dataString = formatDataForAi(results);

        PromptTemplate responsePromptTemplate = new PromptTemplate(
                "Bạn là trợ lý AI cho trang web từ thiện. Dựa trên dữ liệu sau:\n{data}\n" +
                        "Câu hỏi người dùng: {user_query}\n" +
                        "Trả lời bằng tiếng Việt, ngắn gọn, tự nhiên như đang trò chuyện. " +
                        "Nếu có số liệu, giải thích đơn giản. Không đề cập đến SQL hoặc dữ liệu thô.");

        String responsePrompt = responsePromptTemplate.render(Map.of(
                "data", dataString,
                "user_query", userQuery));

        Flux<ChatResponse> responseFlux = chatClient.prompt(responsePrompt).stream().chatResponse();
        streamResponse(responseFlux, sessionId, chat);
    }

    private void generateResponseFromDataOfAIWithUser(String results, String userQuery,
            String sessionId, Chat chat) {

        PromptTemplate responsePromptTemplate = new PromptTemplate(
                "Bạn là trợ lý AI cho trang web từ thiện. Dựa trên dữ liệu sau:\n{data}\n" +
                        "Câu hỏi người dùng: {user_query}\n" +
                        "Trả lời bằng tiếng Việt, ngắn gọn, tự nhiên như đang trò chuyện. " +
                        "Nếu có số liệu, giải thích đơn giản. Không đề cập đến SQL hoặc dữ liệu thô.");

        String responsePrompt = responsePromptTemplate.render(Map.of(
                "data", results,
                "user_query", userQuery));

        Flux<ChatResponse> responseFlux = chatClient.prompt(responsePrompt).stream().chatResponse();
        streamResponse(responseFlux, sessionId, chat);
    }

    /**
     * Stream AI response và lưu vào DB
     */
    private void streamResponse(Flux<ChatResponse> responseFlux, String sessionId, Chat chat) {
        StringBuilder fullResponse = new StringBuilder();

        String messageId = UUID.randomUUID().toString(); // Tạo ID cho message stream

        // Gửi signal bắt đầu streaming
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId,
                Map.of("type", "STREAM_START", "messageId", messageId));

        responseFlux.subscribe(
                response -> {
                    String chunk = response.getResult().getOutput().getText();
                    fullResponse.append(chunk);

                    // GỬI TỪNG CHUNK
                    messagingTemplate.convertAndSend("/topic/chat/" + sessionId,
                            Map.of(
                                    "type", "STREAM_CHUNK",
                                    "messageId", messageId,
                                    "chunk", chunk));
                },
                error -> {
                    messagingTemplate.convertAndSend("/topic/chat/" + sessionId,
                            Map.of("type", "STREAM_ERROR", "messageId", messageId,
                                    "error", error.getMessage()));
                },
                () -> {
                    // Khi hoàn thành, lưu vào DB và gửi signal kết thúc
                    String completeMessage = cleanThinkingTags(fullResponse.toString());
                    MessageDTO savedMessage = saveAiMessage(chat, completeMessage, MessageType.ASSISTANT);

                    messagingTemplate.convertAndSend("/topic/chat/" + sessionId,
                            Map.of(
                                    "type", "STREAM_END",
                                    "messageId", messageId,
                                    "message", savedMessage));
                });
    }

    /**
     * Format dữ liệu cho AI
     */
    private String formatDataForAi(List<Map<String, Object>> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dữ liệu:\n");

        for (int i = 0; i < results.size(); i++) {
            sb.append("Row ").append(i + 1).append(": ");
            Map<String, Object> row = results.get(i);

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Clean AI response
     */
    private String cleanAIResponse(String raw) {
        if (raw == null) {
            return "";
        }

        String cleaned = raw.replaceAll("(?s)<think>.*?</think>", "").trim();
        cleaned = cleaned.replaceAll("(?i)```sql", "").replaceAll("```", "").trim();

        if (cleaned.toUpperCase().contains("SELECT")) {
            int idx = cleaned.toUpperCase().indexOf("SELECT");
            return cleaned.substring(idx).trim();
        }

        return cleaned;
    }

    /**
     * Clean thinking tags
     */
    private String cleanThinkingTags(String text) {
        return text.replaceAll("(?s)<think>.*?</think>", "").trim();
    }

    /**
     * Gửi error message
     */
    private void sendErrorMessage(String sessionId, Chat chat, String errorMessage) {
        MessageDTO errorResponseMessage = saveAiMessage(
                chat,
                errorMessage,
                MessageType.ASSISTANT);
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, errorResponseMessage);
    }

    /**
     * Handle exception
     */
    private void handleError(String sessionId, Chat chat, Exception e) {
        System.err.println("Lỗi trong AiQueryService: " + e.getMessage());
        e.printStackTrace();
        sendErrorMessage(sessionId, chat, "Đã xảy ra lỗi: " + e.getMessage());
    }

}
