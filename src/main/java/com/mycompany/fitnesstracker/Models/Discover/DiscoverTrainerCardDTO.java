package com.mycompany.fitnesstracker.Models.Discover;

import lombok.Builder;

/**
 * Trainer summary for the Discover directory grid. Whitelisted public fields only —
 * never email, phone, password, auth provider, or client/private data.
 */
@Builder
public record DiscoverTrainerCardDTO(
        Long id,
        String firstName,
        String lastName,
        String specialization,
        String imageUrl,
        String gymName
) {
}
