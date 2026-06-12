package com.example.chat_app.controllers;

import com.example.chat_app.dto.dm.DMItemDTO;
import com.example.chat_app.dto.dm.DMSendDTO;
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
import com.example.chat_app.services.DirectMessageService;

import java.security.Principal;

import java.util.HashMap;
import java.util.Map;
import com.example.chat_app.services.OnlineUserService;
import com.example.chat_app.services.UnreadService;
import com.example.chat_app.services.UserService;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final DirectMessageService directMessageService;
    private final UnreadService unreadService;
    private final OnlineUserService onlineUserService;
    private final UserService userService;

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

        // Increment unread for all users except sender
        java.util.Set<String> allUsers = userService.getAllUsernames();
        unreadService.increment(roomId, principal.getName(), allUsers);

        // Notify all users about updated unread counts
        allUsers.forEach(username ->
                messagingTemplate.convertAndSend(
                        "/topic/unread." + username,
                        unreadService.getAll(username)
                )
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

    @MessageMapping("/dm.send/{receiverUsername}")
    public void sendDM(
            @DestinationVariable String receiverUsername,
            @Payload DMSendDTO dto,
            Principal principal) {

        DMItemDTO saved = directMessageService.send(dto, principal.getName(), receiverUsername);

        // Send to receiver
        messagingTemplate.convertAndSend(
                "/topic/dm." + receiverUsername, saved);

        // Send to sender too
        messagingTemplate.convertAndSend(
                "/topic/dm." + principal.getName(), saved);
    }

    @MessageMapping("/chat.react/{roomId}/{messageId}")
    public void reactMessage(
            @DestinationVariable Long roomId,
            @DestinationVariable Long messageId,
            @Payload Map<String, String> payload,
            Principal principal) {

        MessageItemDTO updated = messageService.react(messageId, payload.get("emoji"), principal.getName());
        messagingTemplate.convertAndSend("/topic/room." + roomId, updated);
    }

    @MessageMapping("/chat.pin/{roomId}/{messageId}")
    public void pinMessage(
            @DestinationVariable Long roomId,
            @DestinationVariable Long messageId) {

        MessageItemDTO updated = messageService.togglePin(roomId, messageId);
        // Notify room about pinned message
        messagingTemplate.convertAndSend("/topic/pin." + roomId, updated);
    }
}