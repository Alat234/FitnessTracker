package com.mycompany.fitnesstracker.Models.Discover;

import lombok.Builder;

/**
 * Result of a Discover "connect with trainer" action. Minimal by design — carries no
 * user/connection internals so trainer email/private data is never exposed.
 */
@Builder
public record TrainerConnectResultDTO(String status, String message) {
}
