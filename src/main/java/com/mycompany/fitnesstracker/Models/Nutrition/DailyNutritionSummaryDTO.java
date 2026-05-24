package com.mycompany.fitnesstracker.Models.Nutrition;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Підсумок харчування за день: усі записи + сумарні значення + цілі.
 * Один запит — повна картина для фронту.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyNutritionSummaryDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    private List<NutritionLogDTO> logs;

    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbohydrates;
    private Double totalFat;

    private NutritionGoalDTO goals;
}
