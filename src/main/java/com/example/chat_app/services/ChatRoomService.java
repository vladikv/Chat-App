package com.example.chat_app.services;

import com.example.chat_app.dto.chatroom.ChatRoomCreateDTO;
import com.example.chat_app.dto.chatroom.ChatRoomItemDTO;
import com.example.chat_app.entities.ChatRoomEntity;
import com.example.chat_app.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomItemDTO create(ChatRoomCreateDTO dto) {
        if (chatRoomRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Room already exists");
        }

        ChatRoomEntity room = new ChatRoomEntity();
        room.setName(dto.getName());

        ChatRoomEntity saved = chatRoomRepository.save(room);
        return toDTO(saved);
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
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setCreatedAt(room.getCreatedAt());
        return dto;
    }
}