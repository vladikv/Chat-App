package com.example.chat_app.repositories;

import com.example.chat_app.entities.DirectMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessageEntity, Long> {

    @Query("SELECT m FROM DirectMessageEntity m WHERE " +
            "(m.sender.username = :user1 AND m.receiver.username = :user2) OR " +
            "(m.sender.username = :user2 AND m.receiver.username = :user1) " +
            "ORDER BY m.sentAt ASC")
    List<DirectMessageEntity> findConversation(
            @Param("user1") String user1,
            @Param("user2") String user2);
}