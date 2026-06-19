package com.mycompany.fitnesstracker.Models.Chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * One chat message. {@code fromMe} is computed on the backend relative to the
 * caller (UserDTO carries no id), so the frontend can align bubbles without it.
 */
@Builder
public record ChatMessageDTO(
        Long id,
        String text,
        boolean fromMe,
        String senderName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt
) {
}
