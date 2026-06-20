package com.mycompany.fitnesstracker.Models;

import com.mycompany.fitnesstracker.Models.Enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One in-app notification, stored per recipient (fan-out on create — even system
 * announcements insert one row per targeted user, so read/unread state is simply
 * per-row). {@code system=false} = personal (appointment / connection / chat);
 * {@code system=true} = admin announcement. MVP: no delete, no preferences.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    /** Who sees this notification. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /** Who triggered it (nullable — system announcements have no actor). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType type;

    @Column(name = "notification_title")
    private String title;

    @Column(name = "notification_message", columnDefinition = "TEXT")
    private String message;

    /** Source kind for click-through routing: APPOINTMENT / CONNECTION / CHAT (null for system). */
    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    @Column(name = "notification_created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
