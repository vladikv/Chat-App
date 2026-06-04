package com.example.chat_app.controllers;

import com.example.chat_app.dto.message.MessageItemDTO;
import com.example.chat_app.dto.message.MessageSendDTO;
import com.example.chat_app.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(
            @Payload MessageSendDTO dto,
            @DestinationVariable Long roomId,
            Principal principal) {

        MessageItemDTO saved = messageService.send(dto, roomId, principal.getName());

        messagingTemplate.convertAndSend(
                "/topic/room." + roomId,
                saved
        );
    }
}