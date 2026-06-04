package com.example.chat_app.services;

import com.example.chat_app.dto.message.MessageItemDTO;
import com.example.chat_app.dto.message.MessageSendDTO;
import com.example.chat_app.entities.ChatRoomEntity;
import com.example.chat_app.entities.MessageEntity;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;
    private final UserService userService;

    public MessageItemDTO send(MessageSendDTO dto, Long roomId, String username) {
        ChatRoomEntity room = chatRoomService.findById(roomId);
        UserEntity sender = userService.findByUsername(username);

        MessageEntity message = new MessageEntity();
        message.setContent(dto.getContent());
        message.setSender(sender);
        message.setChatRoom(room);

        MessageEntity saved = messageRepository.save(message);
        return toDTO(saved);
    }

    public List<MessageItemDTO> getByRoom(Long roomId) {
        return messageRepository.findByChatRoomIdOrderBySentAtAsc(roomId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private MessageItemDTO toDTO(MessageEntity message) {
        MessageItemDTO dto = new MessageItemDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setSendAt(message.getSentAt());
        return dto;
    }
}