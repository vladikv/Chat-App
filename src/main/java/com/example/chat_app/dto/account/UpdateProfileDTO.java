package com.example.chat_app.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    @Size(min = 3, max = 20)
    private String username;

    @Email
    private String email;

    private String currentPassword;

    @Size(min = 6)
    private String newPassword;
}