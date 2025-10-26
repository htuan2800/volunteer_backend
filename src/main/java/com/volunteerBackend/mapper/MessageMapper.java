package com.volunteerBackend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.MessageDTO;
import com.volunteerBackend.model.Message;

@Component
public class MessageMapper {

    public MessageDTO toDTO(Message message) {
        if (message == null)
            return null;

        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());

        // Set chatId nếu chat không null
        if (message.getChat() != null) {
            dto.setChatId(message.getChat().getId());
        }

        // Chuyển đổi thông tin user
        if (message.getUser() != null) {
            dto.setUser(true);
            dto.setUserId(message.getUser().getId());
        }


        return dto;
    }


    public MessageDTO toDTOWithTempId(Message message, String tempId) {
        if (message == null)
            return null;

        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setTempId(tempId);

        // Set chatId nếu chat không null
        if (message.getChat() != null) {
            dto.setChatId(message.getChat().getId());
        }

        // Chuyển đổi thông tin user
        if (message.getUser() != null) {
            dto.setUser(true);
            dto.setUserId(message.getUser().getId());
        }

        return dto;
    }



    public List<MessageDTO> toDTOList(List<Message> messages) {
        if (messages == null)
            return Collections.emptyList();

        return messages.stream()
                .map(message -> toDTO(message))
                .collect(Collectors.toList());
    }
}
