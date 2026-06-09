package com.example.chat_app.dto.dm;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DMItemDTO {
    private Long id;
    private String content;
    private String senderUsername;
    private String receiverUsername;
    private LocalDateTime sentAt;
    private boolean edited;
}