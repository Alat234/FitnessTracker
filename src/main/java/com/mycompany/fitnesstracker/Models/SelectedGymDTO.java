package com.mycompany.fitnesstracker.Models;

import lombok.Builder;

/**
 * Compact view of the gym a user selected from Discover, embedded in UserDTO.
 */
@Builder
public record SelectedGymDTO(Long id, String name, String city, String imageUrl) {
}
