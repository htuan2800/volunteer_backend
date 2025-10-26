package com.volunteerBackend.service;
import java.util.List;

import com.volunteerBackend.DTO.ChatDTO;
import com.volunteerBackend.model.Chat;
import com.volunteerBackend.model.User;

public interface ChatService {
    Chat createChatAI(User reqUser);
    Boolean findChatById(Integer chatId, User currentUser) throws Exception;
    ChatDTO getChatWithAI(User reqUser);
    List<ChatDTO> getChatWithUser(User reqUser);
    ChatDTO getChatWithUserContact(User reqUser);
    
}
