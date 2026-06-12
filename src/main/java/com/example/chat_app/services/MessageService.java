package com.example.chat_app.services;

import com.example.chat_app.dto.message.MessageItemDTO;
import com.example.chat_app.dto.message.MessageSendDTO;
import com.example.chat_app.dto.message.MessageUpdateDTO;
import com.example.chat_app.entities.ChatRoomEntity;
import com.example.chat_app.entities.MessageEntity;
import com.example.chat_app.entities.ReactionEntity;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.MessageRepository;
import com.example.chat_app.repositories.ReactionRepository;
import com.example.chat_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;

    private MessageItemDTO toDTO(MessageEntity message) {
        MessageItemDTO dto = new MessageItemDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setSentAt(message.getSentAt());
        dto.setEdited(message.isEdited());
        dto.setPinned(message.isPinned());
        dto.setReplyToId(message.getReplyToId());
        dto.setReplyToSender(message.getReplyToSender());
        dto.setReplyToContent(message.getReplyToContent());

        // Group reactions by emoji
        Map<String, List<String>> reactions = new HashMap<>();
        reactionRepository.findByMessageId(message.getId()).forEach(r -> {
            reactions.computeIfAbsent(r.getEmoji(), k -> new ArrayList<>())
                    .add(r.getUser().getUsername());
        });
        dto.setReactions(reactions);

        return dto;
    }

    @Transactional
    public MessageItemDTO react(Long messageId, String emoji, String username) {

        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        List<ReactionEntity> existing = reactionRepository
                .findByMessageIdAndUserUsername(messageId, username);

        if (!existing.isEmpty() && existing.get(0).getEmoji().equals(emoji)) {
            reactionRepository.deleteAll(existing);
        } else {
            reactionRepository.deleteAll(existing);
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ReactionEntity reaction = new ReactionEntity();
            reaction.setEmoji(emoji);
            reaction.setUser(user);
            reaction.setMessage(message);
            reactionRepository.save(reaction);
        }

        reactionRepository.flush();
        return toDTO(messageRepository.findById(messageId).get());
    }

    public MessageItemDTO send(MessageSendDTO dto, Long roomId, String username) {
        ChatRoomEntity room = chatRoomService.findById(roomId);
        UserEntity sender = userService.findByUsername(username);

        MessageEntity message = new MessageEntity();
        message.setContent(dto.getContent());
        message.setSender(sender);
        message.setChatRoom(room);
        if (dto.getReplyToId() != null) {
            message.setReplyToId(dto.getReplyToId());
            message.setReplyToSender(dto.getReplyToSender());
            message.setReplyToContent(dto.getReplyToContent());
        }

        MessageEntity saved = messageRepository.save(message);
        return toDTO(saved);
    }

    public List<MessageItemDTO> getByRoom(Long roomId) {
        return messageRepository.findByChatRoomIdOrderBySentAtAsc(roomId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<MessageItemDTO> search(Long roomId, String query) {
        return messageRepository.searchByRoomAndContent(roomId, query)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageItemDTO togglePin(Long roomId, Long messageId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        boolean wasPinned = message.isPinned();

        // Unpin all in room first
        messageRepository.findPinnedByRoomId(roomId).ifPresent(m -> {
            m.setPinned(false);
            messageRepository.save(m);
        });

        // If was not pinned — pin it; if was pinned — leave unpinned (toggle off)
        if (!wasPinned) {
            message.setPinned(true);
            return toDTO(messageRepository.save(message));
        }

        // Return unpinned version
        return toDTO(message);
    }

    public java.util.Optional<MessageItemDTO> getPinned(Long roomId) {
        return messageRepository.findPinnedByRoomId(roomId).map(this::toDTO);
    }

    @Transactional
    public void delete(Long messageId, String username) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getUsername().equals(username)) {
            throw new RuntimeException("You can only delete your own messages");
        }

        messageRepository.delete(message);
    }

    @Transactional
    public MessageItemDTO update(Long messageId, MessageUpdateDTO dto, String username) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!messageRepository.findById(messageId)
                .map(m -> m.getSender().getUsername())
                .orElse("").equals(username)) {
            throw new RuntimeException("You can only edit your own messages");
        }

        message.setContent(dto.getContent());
        message.setEdited(true);

        return toDTO(messageRepository.save(message));
    }
}