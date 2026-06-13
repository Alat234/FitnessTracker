package com.mycompany.fitnesstracker.Models.GymEntities;

import com.mycompany.fitnesstracker.Models.Connection.ConnectionUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Тренер у складі залу для відповіді API.
 * id — ідентифікатор зв'язку gym_trainers (для видалення).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymTrainerDTO {
    private Long id;
    private ConnectionUserDTO trainer;
}
