package com.example.chat_app.services;

import com.example.chat_app.dto.dm.DMItemDTO;
import com.example.chat_app.dto.dm.DMSendDTO;
import com.example.chat_app.entities.DirectMessageEntity;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.DirectMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final UserService userService;

    private static final int MAX_PAGE_SIZE = 100;

    @Transactional
    public DMItemDTO send(DMSendDTO dto, String senderUsername, String receiverUsername) {
        UserEntity sender = userService.findByUsername(senderUsername);
        UserEntity receiver = userService.findByUsername(receiverUsername);

        DirectMessageEntity message = new DirectMessageEntity();
        message.setContent(dto.getContent());
        message.setSender(sender);
        message.setReceiver(receiver);
        if (dto.getReplyToId() != null) {
            message.setReplyToId(dto.getReplyToId());
            message.setReplyToSender(dto.getReplyToSender());
            message.setReplyToContent(dto.getReplyToContent());
        }

        return toDTO(directMessageRepository.save(message));
    }

    @Transactional
    public List<DMItemDTO> getConversation(String user1, String user2) {
        return directMessageRepository.findConversation(user1, user2)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cursor-based page of a conversation.
     * Returns up to {@code limit} messages older than {@code beforeId}
     * (or the newest ones when beforeId is null), ordered oldest -> newest.
     */
    @Transactional(readOnly = true)
    public List<DMItemDTO> getConversationPage(String user1, String user2, Long beforeId, int limit) {
        int size = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(0, size);

        List<DirectMessageEntity> page = (beforeId == null)
                ? directMessageRepository.findConversationPage(user1, user2, pageable)
                : directMessageRepository.findConversationBefore(user1, user2, beforeId, pageable);

        List<DMItemDTO> result = page.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        Collections.reverse(result); // fetched newest-first, UI wants oldest-first
        return result;
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
        dto.setReplyToId(message.getReplyToId());
        dto.setReplyToSender(message.getReplyToSender());
        dto.setReplyToContent(message.getReplyToContent());
        return dto;
    }
}