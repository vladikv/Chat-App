package com.example.chat_app.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "reactions")
public class ReactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String emoji;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private MessageEntity message;
}