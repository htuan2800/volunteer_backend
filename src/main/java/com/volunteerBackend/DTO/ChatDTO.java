package com.volunteerBackend.DTO;

import java.time.LocalDateTime;

import com.volunteerBackend.type.ChatType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {
    private Integer id;
    private String chatName;
    private ChatType chatType;
    private String chatImage;
    private LocalDateTime createdAt;
    private String sessionId;
    private int messageCount;
    private MessageDTO lastMessage;
}
