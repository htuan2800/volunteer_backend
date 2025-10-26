package com.volunteerBackend.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    private Integer senderId;
    private String sessionId;
    private String tempId;
    private String text;
    private Integer id;
    private String type;
}
