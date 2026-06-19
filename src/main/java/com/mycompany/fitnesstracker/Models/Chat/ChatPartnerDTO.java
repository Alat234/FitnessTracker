package com.mycompany.fitnesstracker.Models.Chat;

import lombok.Builder;

/**
 * A person the current user is allowed to chat with. {@code support} is true
 * when the conversation is an admin-support thread (one side is an admin).
 */
@Builder
public record ChatPartnerDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean support
) {
}
