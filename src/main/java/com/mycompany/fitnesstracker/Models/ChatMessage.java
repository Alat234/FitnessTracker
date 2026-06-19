package com.mycompany.fitnesstracker.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A text message between two users. Participants are stored as a canonical pair
 * (userLow = lower id, userHigh = higher id) so a conversation is one thread
 * regardless of direction or roles. Allowed pairs: an accepted TRAINER
 * connection, or admin-support (exactly one side ROLE_ADMIN). MVP: no
 * read/delete flags, no attachments.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_low_id")
    private User userLow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_high_id")
    private User userHigh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "message_text", length = 1000)
    private String text;

    @Column(name = "message_created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
