package com.example.chat_app.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * "X is typing..." signal for direct messages.
 * Client sends to /app/dm.typing/{receiver}; only the receiver gets the
 * sender's username on /topic/dm.typing.{receiver}.
 * The sender name comes from the authenticated principal, so it cannot be spoofed.
 */
@Controller
@RequiredArgsConstructor
public class DmTypingController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/dm.typing/{username}")
    public void typing(@DestinationVariable String username, Principal principal) {
        if (principal == null || username == null || username.isBlank()) return;
        if (username.equals(principal.getName())) return; // typing to yourself makes no sense

        messagingTemplate.convertAndSend("/topic/dm.typing." + username, principal.getName());
    }
}