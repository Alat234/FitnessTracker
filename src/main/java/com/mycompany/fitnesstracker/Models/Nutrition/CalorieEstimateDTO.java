package com.mycompany.fitnesstracker.Models.Nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of the BMR/TDEE calorie estimate. When {@code complete} is false the
 * calorie fields are null and {@code message} explains what data is missing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalorieEstimateDTO {
    private Integer bmr;
    private Integer tdee;
    private Integer maintainCalories;
    private Integer loseCalories;
    private Integer gainCalories;
    private Integer selectedGoalCalories;
    private boolean complete;
    private String message;
}
