package com.mycompany.fitnesstracker.Models.Connection;

import com.mycompany.fitnesstracker.Models.Nutrition.DailyNutritionSummaryDTO;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOutDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Прогрес власника даних для перегляду viewer-ом (друг або тренер).
 * Розділи, на які немає права, повертаються як null;
 * блок permissions завжди присутній і каже фронтенду, що рендерити.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedProgressDTO {

    private ConnectionUserDTO owner;
    private PermissionsDTO permissions;

    /* null, якщо немає права workouts */
    private List<WorkOutDTO> workoutHistory;

    /* null, якщо немає права progressSummary */
    private Integer totalWorkouts;
    private Double totalVolumeKg;

    /* null, якщо немає права nutrition */
    private DailyNutritionSummaryDTO todayNutrition;

    /* Зарезервовано: метрики тіла ще не реалізовані на бекенді — завжди null у цій фазі */
    private Object bodyMetrics;
}
