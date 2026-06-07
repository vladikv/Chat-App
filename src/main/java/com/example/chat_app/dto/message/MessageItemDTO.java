package com.example.chat_app.dto.message;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageItemDTO {
    private Long id;
    private String content;
    private String senderUsername;
    private Long chatRoomId;
    private LocalDateTime sentAt;
    private boolean edited;
}
