package com.example.chat_app.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageSendDTO {

    @NotBlank(message = "Message cannot be empty")
    private String content;
}