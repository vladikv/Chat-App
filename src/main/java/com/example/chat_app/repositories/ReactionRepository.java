package com.example.chat_app.repositories;

import com.example.chat_app.entities.ReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReactionRepository extends JpaRepository<ReactionEntity, Long> {
    List<ReactionEntity> findByMessageId(Long messageId);
    List<ReactionEntity> findByMessageIdAndUserUsername(Long messageId, String username);
    void deleteByMessageIdAndUserUsername(Long messageId, String username);
}