package com.example.chat_app.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DmTypingControllerTest {

    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private DmTypingController controller;

    @Test
    void typing_notifiesReceiverWithSenderName() {
        controller.typing("bob", () -> "alice");

        verify(messagingTemplate).convertAndSend("/topic/dm.typing.bob", (Object) "alice");
    }

    @Test
    void typing_ignoresTypingToYourself() {
        controller.typing("alice", () -> "alice");

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void typing_ignoresMissingPrincipal() {
        controller.typing("bob", null);

        verifyNoInteractions(messagingTemplate);
    }
}