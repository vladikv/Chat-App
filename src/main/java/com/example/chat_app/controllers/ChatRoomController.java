package com.example.chat_app.controllers;

import com.example.chat_app.dto.chatroom.ChatRoomCreateDTO;
import com.example.chat_app.dto.chatroom.ChatRoomItemDTO;
import com.example.chat_app.entities.ChatRoomEntity;
import com.example.chat_app.repositories.ChatRoomRepository;
import com.example.chat_app.services.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;

    @PostMapping
    public ResponseEntity<ChatRoomItemDTO> create(
            @Valid @RequestBody ChatRoomCreateDTO dto,
            Principal principal) {
        return ResponseEntity.ok(chatRoomService.create(dto, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomItemDTO>> getAll() {
        return ResponseEntity.ok(chatRoomService.getAll());
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<ChatRoomItemDTO> rename(
            @PathVariable Long roomId,
            @RequestBody ChatRoomCreateDTO dto,
            Principal principal) {
        return ResponseEntity.ok(chatRoomService.rename(roomId, dto.getName(), principal.getName()));
    }
}