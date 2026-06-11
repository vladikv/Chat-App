package com.example.chat_app.controllers;

import com.example.chat_app.services.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import com.example.chat_app.dto.account.UserItemDTO;
import com.example.chat_app.services.UserService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OnlineUserController {

    private final OnlineUserService onlineUserService;
    private final UserService userService;

    @GetMapping("/online-users")
    public ResponseEntity<Set<String>> getOnlineUsers() {
        return ResponseEntity.ok(onlineUserService.getOnlineUsers());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserItemDTO>> getAllUsers() {
        Set<String> online = onlineUserService.getOnlineUsers();
        List<UserItemDTO> users = userService.getAll().stream()
                .map(u -> {
                    UserItemDTO dto = new UserItemDTO();
                    dto.setId(u.getId());
                    dto.setUsername(u.getUsername());
                    dto.setOnline(online.contains(u.getUsername()));
                    dto.setAvatarUrl(u.getAvatarUrl());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

}