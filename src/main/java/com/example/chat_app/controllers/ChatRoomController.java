package com.example.chat_app.controllers;

import com.example.chat_app.dto.chatroom.ChatRoomCreateDTO;
import com.example.chat_app.dto.chatroom.ChatRoomItemDTO;
import com.example.chat_app.services.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoomItemDTO> create(@Valid @RequestBody ChatRoomCreateDTO dto) {
        return ResponseEntity.ok(chatRoomService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomItemDTO>> getAll() {
        return ResponseEntity.ok(chatRoomService.getAll());
    }
}