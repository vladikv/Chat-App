package com.example.chat_app.services;

import com.example.chat_app.dto.chatroom.ChatRoomCreateDTO;
import com.example.chat_app.dto.chatroom.ChatRoomItemDTO;
import com.example.chat_app.entities.ChatRoomEntity;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.ChatRoomRepository;
import com.example.chat_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public ChatRoomItemDTO rename(Long roomId, String newName, String username) {
        ChatRoomEntity room = findById(roomId);

        if (room.getCreatedBy() == null || !room.getCreatedBy().getUsername().equals(username)) {
            throw new RuntimeException("Only the creator can rename this room");
        }

        if (chatRoomRepository.existsByName(newName)) {
            throw new RuntimeException("Room name already taken");
        }

        room.setName(newName);
        return toDTO(chatRoomRepository.save(room));
    }

    public ChatRoomItemDTO create(ChatRoomCreateDTO dto, String username) {
        if (chatRoomRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Room already exists");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoomEntity room = new ChatRoomEntity();
        room.setName(dto.getName());
        room.setCreatedBy(user);

        return toDTO(chatRoomRepository.save(room));
    }

    public List<ChatRoomItemDTO> getAll() {
        return chatRoomRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ChatRoomEntity findById(Long id) {
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    private ChatRoomItemDTO toDTO(ChatRoomEntity room) {
        ChatRoomItemDTO dto = new ChatRoomItemDTO();
        if (room.getCreatedBy() != null) {
            dto.setCreatedBy(room.getCreatedBy().getUsername());
        }
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setCreatedAt(room.getCreatedAt());
        return dto;
    }
}