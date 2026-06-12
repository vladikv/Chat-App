package com.example.chat_app.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "edited", columnDefinition = "boolean default false")
    private boolean edited;

    @Column(name = "pinned", columnDefinition = "boolean default false")
    private boolean pinned;

    @Column(name = "reply_to_id")
    private Long replyToId;

    @Column(name = "reply_to_sender")
    private String replyToSender;

    @Column(name = "reply_to_content", length = 500)
    private String replyToContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoomEntity chatRoom;

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ReactionEntity> reactions = new ArrayList<>();
}