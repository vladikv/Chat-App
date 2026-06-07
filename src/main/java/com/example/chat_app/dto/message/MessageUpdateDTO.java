package com.example.chat_app.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageUpdateDTO {
    @NotBlank
    private String content;
}