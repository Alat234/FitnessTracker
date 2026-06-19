package com.mycompany.fitnesstracker.Models.Appointment;

import lombok.Builder;

/**
 * Minimal user summary for an appointment. Trainer and client are already
 * connected, so name + email are safe to expose (consistent with ClientDTO).
 */
@Builder
public record AppointmentPartyDTO(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}
