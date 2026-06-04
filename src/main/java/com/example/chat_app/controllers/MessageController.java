package com.example.chat_app.controllers;

import com.example.chat_app.dto.message.MessageItemDTO;
import com.example.chat_app.dto.message.MessageSendDTO;
import com.example.chat_app.services.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<MessageItemDTO> send(
            @PathVariable Long roomId,
            @Valid @RequestBody MessageSendDTO dto,
            Principal principal) {
        return ResponseEntity.ok(messageService.send(dto, roomId, principal.getName()));
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<MessageItemDTO>> getByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(messageService.getByRoom(roomId));
    }
}