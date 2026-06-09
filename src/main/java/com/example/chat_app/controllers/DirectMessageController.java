package com.example.chat_app.controllers;

import com.example.chat_app.dto.dm.DMItemDTO;
import com.example.chat_app.dto.dm.DMSendDTO;
import com.example.chat_app.services.DirectMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    @GetMapping("/{username}")
    public ResponseEntity<List<DMItemDTO>> getConversation(
            @PathVariable String username,
            Principal principal) {
        return ResponseEntity.ok(directMessageService.getConversation(
                principal.getName(), username));
    }

    @PostMapping("/{username}")
    public ResponseEntity<DMItemDTO> send(
            @PathVariable String username,
            @Valid @RequestBody DMSendDTO dto,
            Principal principal) {
        return ResponseEntity.ok(directMessageService.send(dto, principal.getName(), username));
    }

    @DeleteMapping("/{username}/{messageId}")
    public ResponseEntity<Void> delete(
            @PathVariable String username,
            @PathVariable Long messageId,
            Principal principal) {
        directMessageService.delete(messageId, principal.getName());
        return ResponseEntity.ok().build();
    }
}