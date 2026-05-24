package com.mycompany.fitnesstracker.Models.Trainer;

import com.mycompany.fitnesstracker.Models.Nutrition.DailyNutritionSummaryDTO;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOutDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Прогрес клієнта — для перегляду тренером.
 * Об'єднує історію тренувань і денний підсумок харчування на сьогодні.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProgressDTO {
    private ClientDTO                 client;
    private List<WorkOutDTO>          workoutHistory;
    private Integer                   totalWorkouts;
    private Double                    totalVolumeKg;
    private DailyNutritionSummaryDTO  todayNutrition;
}
