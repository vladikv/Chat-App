package com.example.chat_app.dto.chatroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRoomCreateDTO {

    @NotBlank(message = "Room name is required")
    @Size(min = 3, max = 50, message = "Room name must be between 3 and 50 characters")
    private String name;
}