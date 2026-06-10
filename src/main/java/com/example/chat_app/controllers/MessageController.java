package com.example.chat_app.controllers;

import com.example.chat_app.dto.message.MessageItemDTO;
import com.example.chat_app.dto.message.MessageSendDTO;
import com.example.chat_app.dto.message.MessageUpdateDTO;
import com.example.chat_app.services.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.chat_app.services.UnreadService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UnreadService unreadService;

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

    @PostMapping("/{roomId}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long roomId,
            Principal principal) {
        unreadService.reset(roomId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}/messages/search")
    public ResponseEntity<List<MessageItemDTO>> search(
            @PathVariable Long roomId,
            @RequestParam String q) {
        return ResponseEntity.ok(messageService.search(roomId, q));
    }

    @DeleteMapping("/{roomId}/messages/{messageId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long roomId,
            @PathVariable Long messageId,
            Principal principal) {
        messageService.delete(messageId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{roomId}/messages/{messageId}")
    public ResponseEntity<MessageItemDTO> update(
            @PathVariable Long roomId,
            @PathVariable Long messageId,
            @Valid @RequestBody MessageUpdateDTO dto,
            Principal principal) {
        return ResponseEntity.ok(messageService.update(messageId, dto, principal.getName()));
    }
}