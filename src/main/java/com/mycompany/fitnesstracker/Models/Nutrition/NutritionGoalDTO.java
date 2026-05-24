package com.mycompany.fitnesstracker.Models.Nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionGoalDTO {
    private Integer calorieGoal;
    private Integer proteinGoal;
    private Integer carbsGoal;
    private Integer fatGoal;
}
