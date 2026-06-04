package com.example.chat_app.repositories;

import com.example.chat_app.entities.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);
}