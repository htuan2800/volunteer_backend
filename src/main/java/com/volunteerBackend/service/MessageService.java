package com.volunteerBackend.service;

import java.util.List;

import org.springframework.ai.chat.messages.MessageType;

import com.volunteerBackend.DTO.MessageDTO;
import com.volunteerBackend.model.Chat;
import com.volunteerBackend.model.User;
import com.volunteerBackend.request.MessageRequest;

public interface MessageService {
    MessageDTO createMessage(MessageRequest req) throws Exception;
    void processUserMessage(MessageRequest req, User currentUser);
    MessageDTO saveAiMessage(Chat chat, String content, MessageType messageType);
    List<MessageDTO> findChatsMessages(Integer chatId, User user) throws Exception;
}
