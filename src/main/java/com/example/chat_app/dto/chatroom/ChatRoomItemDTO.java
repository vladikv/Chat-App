package com.example.chat_app.dto.chatroom;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatRoomItemDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}