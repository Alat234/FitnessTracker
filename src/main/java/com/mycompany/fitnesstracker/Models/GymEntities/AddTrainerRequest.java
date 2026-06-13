package com.mycompany.fitnesstracker.Models.GymEntities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Тіло запиту на додавання тренера до залу за email.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddTrainerRequest {
    private String email;
}
