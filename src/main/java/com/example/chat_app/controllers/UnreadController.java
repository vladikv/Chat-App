package com.example.chat_app.controllers;

import com.example.chat_app.services.UnreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/unread")
@RequiredArgsConstructor
public class UnreadController {

    private final UnreadService unreadService;

    @GetMapping
    public ResponseEntity<Map<String, Integer>> getUnread(Principal principal) {
        return ResponseEntity.ok(unreadService.getAll(principal.getName()));
    }
}