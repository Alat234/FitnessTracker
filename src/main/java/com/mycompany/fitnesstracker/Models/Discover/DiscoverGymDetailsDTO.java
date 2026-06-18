package com.mycompany.fitnesstracker.Models.Discover;

import lombok.Builder;

import java.util.List;

/**
 * Public gym details for Discover. Gym business contact (phone/email) is the gym's
 * own, intentionally shown. No gym owner user, no trainer emails, no client data.
 */
@Builder
public record DiscoverGymDetailsDTO(
        Long id,
        String name,
        String description,
        String city,
        String address,
        String phoneNumber,
        String email,
        String imageUrl,
        long trainerCount,
        List<DiscoverTrainerSummaryDTO> trainers
) {
}
