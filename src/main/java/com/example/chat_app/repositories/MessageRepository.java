package com.example.chat_app.repositories;

import com.example.chat_app.entities.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);
    @Query("SELECT m FROM MessageEntity m WHERE m.chatRoom.id = :roomId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY m.sentAt ASC")
    List<MessageEntity> searchByRoomAndContent(@Param("roomId") Long roomId, @Param("query") String query);
}
