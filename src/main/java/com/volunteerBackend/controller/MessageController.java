package com.volunteerBackend.controller;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.volunteerBackend.DTO.MessageDTO;
import com.volunteerBackend.model.User;
import com.volunteerBackend.request.MessageRequest;
import com.volunteerBackend.service.MessageService;
import com.volunteerBackend.service.UserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
@RestController
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    private final SimpMessagingTemplate messagingTemplate;
    @PersistenceContext
    private EntityManager entityManager;

    public MessageController(
            SimpMessagingTemplate messagingTemplate, EntityManager entityManager) {
        this.messagingTemplate = messagingTemplate;
        this.entityManager = entityManager;
    }

    @MessageMapping("/chat/send")
    @Transactional
    public void createMessage(@Payload MessageRequest req)
            throws IOException {
        String sessionId = req.getSessionId();

        try {

           messageService.processUserMessage(req);

        } catch (Exception e) {
            System.err.println("Lỗi trong createMessage: " + e.getMessage());
            e.printStackTrace();
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId, "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/api/messages/chat/{chatBoxId}")
    public List<MessageDTO> findChatMessage(@RequestHeader("Authorization") String jwt, @PathVariable Integer chatBoxId)
            throws Exception {
        User user = userService.findUserByJwt(jwt);
        List<MessageDTO> messages = messageService.findChatsMessages(chatBoxId, user);
        return messages;
    }
}
