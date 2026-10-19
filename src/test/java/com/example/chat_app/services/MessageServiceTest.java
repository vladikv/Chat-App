package com.example.chat_app.services;

import com.example.chat_app.dto.message.MessageItemDTO;
import com.example.chat_app.dto.message.MessageSendDTO;
import com.example.chat_app.dto.message.MessageUpdateDTO;
import com.example.chat_app.entities.ChatRoomEntity;
import com.example.chat_app.entities.MessageEntity;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.MessageRepository;
import com.example.chat_app.repositories.ReactionRepository;
import com.example.chat_app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private ChatRoomService chatRoomService;
    @Mock private UserService userService;
    @Mock private ReactionRepository reactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private MessageService messageService;

    private UserEntity user;
    private ChatRoomEntity room;
    private MessageEntity message;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");

        room = new ChatRoomEntity();
        room.setId(1L);
        room.setName("general");

        message = new MessageEntity();
        message.setId(1L);
        message.setContent("Hello!");
        message.setSender(user);
        message.setChatRoom(room);
        message.setSentAt(LocalDateTime.now());
    }

    @Test
    void send_shouldSaveAndReturnMessage() {
        MessageSendDTO dto = new MessageSendDTO();
        dto.setContent("Hello!");

        when(chatRoomService.findById(1L)).thenReturn(room);
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(messageRepository.save(any())).thenReturn(message);
        when(reactionRepository.findByMessageId(1L)).thenReturn(Collections.emptyList());

        MessageItemDTO result = messageService.send(dto, 1L, "testuser");

        assertThat(result.getContent()).isEqualTo("Hello!");
        assertThat(result.getSenderUsername()).isEqualTo("testuser");
        verify(messageRepository).save(any());
    }

    @Test
    void send_shouldSetReplyFields() {
        MessageSendDTO dto = new MessageSendDTO();
        dto.setContent("Reply!");
        dto.setReplyToId(5L);
        dto.setReplyToSender("otheruser");
        dto.setReplyToContent("Original");

        message.setReplyToId(5L);
        message.setReplyToSender("otheruser");
        message.setReplyToContent("Original");

        when(chatRoomService.findById(1L)).thenReturn(room);
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(messageRepository.save(any())).thenReturn(message);
        when(reactionRepository.findByMessageId(1L)).thenReturn(Collections.emptyList());

        MessageItemDTO result = messageService.send(dto, 1L, "testuser");

        assertThat(result.getReplyToId()).isEqualTo(5L);
        assertThat(result.getReplyToSender()).isEqualTo("otheruser");
    }

    @Test
    void delete_shouldDeleteSuccessfully() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        messageService.delete(1L, "testuser");

        verify(messageRepository).delete(message);
    }

    @Test
    void delete_shouldThrowWhenNotOwner() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.delete(1L, "otheruser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You can only delete your own messages");
    }

    @Test
    void update_shouldUpdateContent() {
        MessageUpdateDTO dto = new MessageUpdateDTO();
        dto.setContent("Updated!");

        message.setContent("Updated!");
        message.setEdited(true);

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any())).thenReturn(message);
        when(reactionRepository.findByMessageId(1L)).thenReturn(Collections.emptyList());

        MessageItemDTO result = messageService.update(1L, dto, "testuser");

        assertThat(result.getContent()).isEqualTo("Updated!");
        assertThat(result.isEdited()).isTrue();
    }

    @Test
    void update_shouldThrowWhenNotOwner() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.update(1L, new MessageUpdateDTO(), "otheruser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You can only edit your own messages");
    }

    @Test
    void getByRoom_shouldReturnMessages() {
        when(messageRepository.findByChatRoomIdOrderBySentAtAsc(1L))
                .thenReturn(List.of(message));
        when(reactionRepository.findByMessageId(1L)).thenReturn(Collections.emptyList());

        List<MessageItemDTO> result = messageService.getByRoom(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Hello!");
    }

    @Test
    void togglePin_shouldPinMessage() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.findPinnedByRoomId(1L)).thenReturn(Optional.empty());
        when(reactionRepository.findByMessageId(1L)).thenReturn(Collections.emptyList());

        // message.isPinned() is false by default, so togglePin should pin it
        message.setPinned(false);
        MessageEntity pinnedMessage = new MessageEntity();
        pinnedMessage.setId(1L);
        pinnedMessage.setContent("Hello!");
        pinnedMessage.setSender(user);
        pinnedMessage.setChatRoom(room);
        pinnedMessage.setSentAt(LocalDateTime.now());
        pinnedMessage.setPinned(true);

        when(messageRepository.save(any())).thenReturn(pinnedMessage);

        MessageItemDTO result = messageService.togglePin(1L, 1L);

        assertThat(result.isPinned()).isTrue();
        verify(messageRepository).save(any());
    }

    @Test
    void togglePin_shouldUnpinWhenAlreadyPinned() {
        message.setPinned(true);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.findPinnedByRoomId(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any())).thenReturn(message);
        when(reactionRepository.findByMessageId(1L)).thenReturn(Collections.emptyList());

        MessageItemDTO result = messageService.togglePin(1L, 1L);

        assertThat(result.isPinned()).isFalse();
    }
}