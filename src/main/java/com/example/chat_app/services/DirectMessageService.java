package com.example.chat_app.services;

import com.example.chat_app.dto.dm.DMItemDTO;
import com.example.chat_app.dto.dm.DMSendDTO;
import com.example.chat_app.entities.DirectMessageEntity;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.DirectMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final UserService userService;

    @Transactional
    public DMItemDTO send(DMSendDTO dto, String senderUsername, String receiverUsername) {
        UserEntity sender = userService.findByUsername(senderUsername);
        UserEntity receiver = userService.findByUsername(receiverUsername);

        DirectMessageEntity message = new DirectMessageEntity();
        message.setContent(dto.getContent());
        message.setSender(sender);
        message.setReceiver(receiver);

        return toDTO(directMessageRepository.save(message));
    }

    @Transactional
    public List<DMItemDTO> getConversation(String user1, String user2) {
        return directMessageRepository.findConversation(user1, user2)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long messageId, String username) {
        DirectMessageEntity message = directMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getUsername().equals(username)) {
            throw new RuntimeException("You can only delete your own messages");
        }

        directMessageRepository.delete(message);
    }

    private DMItemDTO toDTO(DirectMessageEntity message) {
        DMItemDTO dto = new DMItemDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setReceiverUsername(message.getReceiver().getUsername());
        dto.setSentAt(message.getSentAt());
        dto.setEdited(message.isEdited());
        return dto;
    }
}