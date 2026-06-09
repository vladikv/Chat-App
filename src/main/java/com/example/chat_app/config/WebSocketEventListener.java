package com.example.chat_app.config;

import com.example.chat_app.services.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        Principal user = event.getUser();
        System.out.println("WS Connect: user=" + (user != null ? user.getName() : "NULL"));
        if (user != null) {
            onlineUserService.addUser(user.getName());
            broadcastOnlineUsers();
        }
    }
    
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            onlineUserService.removeUser(user.getName());
            broadcastOnlineUsers();
        }
    }

    private void broadcastOnlineUsers() {
        messagingTemplate.convertAndSend("/topic/online-users",
                onlineUserService.getOnlineUsers());
    }
}