package com.example.chat_app.controllers;

import com.example.chat_app.dto.message.MessageItemDTO;
import com.example.chat_app.dto.message.MessageSendDTO;
import com.example.chat_app.dto.message.MessageUpdateDTO;
import com.example.chat_app.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

import java.util.HashMap;
import java.util.Map;

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

    @MessageMapping("/chat.delete/{roomId}/{messageId}")
    public void deleteMessage(
            @DestinationVariable Long roomId,
            @DestinationVariable Long messageId,
            Principal principal) {

        messageService.delete(messageId, principal.getName());

        Map<String, Long> payload = new HashMap<>();
        payload.put("deletedId", messageId);

        messagingTemplate.convertAndSend("/topic/room." + roomId, payload);
    }

    @MessageMapping("/typing/{roomId}")
    public void typing(@DestinationVariable Long roomId, Principal principal) {
        messagingTemplate.convertAndSend(
                "/topic/typing." + roomId,
                principal.getName()
        );
    }


    @MessageMapping("/chat.edit/{roomId}/{messageId}")
    public void editMessage(
            @DestinationVariable Long roomId,
            @DestinationVariable Long messageId,
            @Payload MessageUpdateDTO dto,
            Principal principal) {

        MessageItemDTO updated = messageService.update(messageId, dto, principal.getName());
        messagingTemplate.convertAndSend("/topic/room." + roomId, updated);
    }
}