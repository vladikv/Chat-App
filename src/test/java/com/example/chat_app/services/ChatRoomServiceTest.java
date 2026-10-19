package com.example.chat_app.services;

import com.example.chat_app.dto.chatroom.ChatRoomCreateDTO;
import com.example.chat_app.dto.chatroom.ChatRoomItemDTO;
import com.example.chat_app.entities.ChatRoomEntity;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.ChatRoomRepository;
import com.example.chat_app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ChatRoomService chatRoomService;

    private UserEntity user;
    private ChatRoomEntity room;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("encoded");

        room = new ChatRoomEntity();
        room.setId(1L);
        room.setName("general");
        room.setCreatedBy(user);
    }

    @Test
    void create_shouldCreateRoomSuccessfully() {
        ChatRoomCreateDTO dto = new ChatRoomCreateDTO();
        dto.setName("general");

        when(chatRoomRepository.existsByName("general")).thenReturn(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(chatRoomRepository.save(any())).thenReturn(room);

        ChatRoomItemDTO result = chatRoomService.create(dto, "testuser");

        assertThat(result.getName()).isEqualTo("general");
        assertThat(result.getCreatedBy()).isEqualTo("testuser");
        verify(chatRoomRepository).save(any());
    }

    @Test
    void create_shouldThrowWhenRoomAlreadyExists() {
        ChatRoomCreateDTO dto = new ChatRoomCreateDTO();
        dto.setName("general");

        when(chatRoomRepository.existsByName("general")).thenReturn(true);

        assertThatThrownBy(() -> chatRoomService.create(dto, "testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Room already exists");
    }

    @Test
    void rename_shouldRenameSuccessfully() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(chatRoomRepository.existsByName("new-name")).thenReturn(false);
        room.setName("new-name");
        when(chatRoomRepository.save(room)).thenReturn(room);

        ChatRoomItemDTO result = chatRoomService.rename(1L, "new-name", "testuser");

        assertThat(result.getName()).isEqualTo("new-name");
    }

    @Test
    void rename_shouldThrowWhenNotCreator() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatRoomService.rename(1L, "new-name", "otheruser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only the creator can rename this room");
    }

    @Test
    void rename_shouldThrowWhenNameTaken() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(chatRoomRepository.existsByName("taken")).thenReturn(true);

        assertThatThrownBy(() -> chatRoomService.rename(1L, "taken", "testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Room name already taken");
    }

    @Test
    void delete_shouldThrowWhenNotCreator() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatRoomService.delete(1L, "otheruser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only the creator can delete this room");
    }

    @Test
    void delete_shouldDeleteSuccessfully() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

        chatRoomService.delete(1L, "testuser");

        verify(chatRoomRepository).delete(room);
    }

    @Test
    void getAll_shouldReturnAllRooms() {
        when(chatRoomRepository.findAll()).thenReturn(List.of(room));

        List<ChatRoomItemDTO> result = chatRoomService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("general");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(chatRoomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Room not found");
    }
}