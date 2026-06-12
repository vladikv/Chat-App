package com.example.chat_app.dto.dm;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DMSendDTO {
    @NotBlank
    private String content;

    private Long replyToId;
    private String replyToSender;
    private String replyToContent;
}