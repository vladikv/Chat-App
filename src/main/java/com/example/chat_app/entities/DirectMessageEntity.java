package com.example.chat_app.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "direct_messages")
public class DirectMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "edited", columnDefinition = "boolean default false")
    private boolean edited;

    @Column(name = "reply_to_id")
    private Long replyToId;

    @Column(name = "reply_to_sender")
    private String replyToSender;

    @Column(name = "reply_to_content", length = 500)
    private String replyToContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;
}