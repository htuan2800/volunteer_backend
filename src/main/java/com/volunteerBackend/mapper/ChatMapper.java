package com.volunteerBackend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.ChatDTO;
import com.volunteerBackend.config.FileStorageProperties;
import com.volunteerBackend.model.Chat;
import com.volunteerBackend.model.Message;
import com.volunteerBackend.type.AuthProvider;
import com.volunteerBackend.type.ChatType;

@Component
public class ChatMapper {

    private final FileStorageProperties fileStorageProperties;

    public ChatMapper(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Autowired
    private MessageMapper messageMapper;

    public ChatDTO toDTO(Chat chat) {
        if (chat == null)
            return null;

        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setSessionId(chat.getSessionId());
        if (chat.getChatType().equals(ChatType.AI)) {
            dto.setChatName(chat.getChatName());
        } else {
            dto.setChatName(chat.getUser().getFullName());
            if (chat.getUser().getAvatar().startsWith("http")) {
                dto.setChatImage(chat.getUser().getAvatar());
            } else {
                dto.setChatImage(fileStorageProperties.getBaseUrl() + chat.getUser().getAvatar());
            }
            dto.setCreatedAt(chat.getCreatedAt());
            List<Message> messages = chat.getMessages();

            if (messages != null && !messages.isEmpty()) {
                Message lastMessage = messages.get(messages.size() - 1);
                dto.setLastMessage(messageMapper.toDTO(lastMessage));
            } else {
                dto.setLastMessage(null); // Hoặc một tin nhắn mặc định
            }
        }

        return dto;
    }

    public List<ChatDTO> toDTOList(List<Chat> chats) {
        if (chats == null)
            return Collections.emptyList();

        return chats.stream()
                .map(chat -> toDTO(chat))
                .collect(Collectors.toList());
    }
}
