package com.example.chat_app.dto.account;

import lombok.Data;

@Data
public class UserItemDTO {
    private Long id;
    private String username;
    private boolean online;
}