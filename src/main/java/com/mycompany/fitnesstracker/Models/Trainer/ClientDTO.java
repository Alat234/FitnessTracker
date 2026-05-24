package com.mycompany.fitnesstracker.Models.Trainer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Мінімальна інформація про клієнта тренера —
 * для списку клієнтів у вкладці «Clients».
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Integer totalWorkouts;
}
