package com.mycompany.fitnesstracker.Models.Notifications;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mycompany.fitnesstracker.Models.Enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * One notification for the current user. {@code system} drives the frontend
 * Personal/System split (false = personal, true = admin announcement);
 * {@code entityType}/{@code entityId} drive click-through routing.
 */
@Builder
public record NotificationDTO(
        Long id,
        NotificationType type,
        String title,
        String message,
        String entityType,
        Long entityId,
        boolean isRead,
        boolean system,
        String actorName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt
) {
}
