package com.example.chat_app.dto.account;

import lombok.Data;

@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private String displayUsername;
    private String email;
    private String avatarColor;
}