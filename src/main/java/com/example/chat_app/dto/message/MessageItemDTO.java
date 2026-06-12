package com.example.chat_app.dto.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class MessageItemDTO {
    private Map<String, List<String>> reactions;
    private Long id;
    private String content;
    private String senderUsername;
    private Long chatRoomId;
    private LocalDateTime sentAt;
    private boolean edited;
    private boolean pinned;
    private Long replyToId;
    private String replyToSender;
    private String replyToContent;
}
