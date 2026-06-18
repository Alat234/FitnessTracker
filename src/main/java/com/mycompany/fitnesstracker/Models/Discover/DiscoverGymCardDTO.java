package com.mycompany.fitnesstracker.Models.Discover;

import lombok.Builder;

/**
 * Gym summary for the Discover directory grid. Whitelisted public fields only —
 * no owner, no contact emails of users.
 */
@Builder
public record DiscoverGymCardDTO(
        Long id,
        String name,
        String city,
        String address,
        String imageUrl,
        long trainerCount
) {
}
