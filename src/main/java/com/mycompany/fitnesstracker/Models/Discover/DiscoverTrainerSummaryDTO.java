package com.mycompany.fitnesstracker.Models.Discover;

import lombok.Builder;

/**
 * Public trainer summary for a gym's Discover page. Name only — never exposes
 * email, id, or any private/client data.
 */
@Builder
public record DiscoverTrainerSummaryDTO(String firstName, String lastName) {
}
