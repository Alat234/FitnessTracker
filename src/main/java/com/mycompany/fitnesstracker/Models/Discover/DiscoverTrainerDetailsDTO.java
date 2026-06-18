package com.mycompany.fitnesstracker.Models.Discover;

import lombok.Builder;

/**
 * Public trainer profile for Discover. Whitelisted public fields only — never email,
 * phone, password, auth provider, or client/private progress data.
 */
@Builder
public record DiscoverTrainerDetailsDTO(
        Long id,
        String firstName,
        String lastName,
        String bio,
        String specialization,
        String imageUrl,
        String gymName
) {
}
