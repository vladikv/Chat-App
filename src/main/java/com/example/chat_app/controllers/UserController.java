package com.example.chat_app.controllers;

import com.example.chat_app.dto.account.UpdateProfileDTO;
import com.example.chat_app.dto.account.UserProfileDTO;
import com.example.chat_app.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfileDTO> getProfile(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getName()));
    }

    @PutMapping
    public ResponseEntity<UserProfileDTO> updateProfile(
            @Valid @RequestBody UpdateProfileDTO dto,
            Principal principal) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), dto));
    }

    @PutMapping("/avatar")
    public ResponseEntity<UserProfileDTO> updateAvatar(
            @RequestBody java.util.Map<String, String> body,
            Principal principal) {
        return ResponseEntity.ok(userService.updateAvatar(principal.getName(), body.get("avatarUrl")));
    }
}