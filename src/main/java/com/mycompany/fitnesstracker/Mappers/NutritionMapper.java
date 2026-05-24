package com.mycompany.fitnesstracker.Mappers;

import com.mycompany.fitnesstracker.Models.Nutrition.NutritionGoal;
import com.mycompany.fitnesstracker.Models.Nutrition.NutritionGoalDTO;
import com.mycompany.fitnesstracker.Models.Nutrition.NutritionLog;
import com.mycompany.fitnesstracker.Models.Nutrition.NutritionLogDTO;
import org.springframework.stereotype.Component;

@Component
public class NutritionMapper {

    /* ── NutritionLog ↔ NutritionLogDTO ────────────────────── */
    public NutritionLogDTO toDTO(NutritionLog entity) {
        if (entity == null) return null;
        return NutritionLogDTO.builder()
                .id(entity.getId())
                .foodName(entity.getFoodName())
                .quantity(entity.getQuantity())
                .unit(entity.getUnit())
                .calories(entity.getCalories())
                .protein(entity.getProtein())
                .carbohydrates(entity.getCarbohydrates())
                .fat(entity.getFat())
                .date(entity.getDate())
                .build();
    }

    public NutritionLog toEntity(NutritionLogDTO dto) {
        if (dto == null) return null;
        return NutritionLog.builder()
                .foodName(dto.getFoodName())
                .quantity(dto.getQuantity())
                .unit(dto.getUnit())
                .calories(dto.getCalories() != null ? dto.getCalories() : 0.0)
                .protein(dto.getProtein() != null ? dto.getProtein() : 0.0)
                .carbohydrates(dto.getCarbohydrates() != null ? dto.getCarbohydrates() : 0.0)
                .fat(dto.getFat() != null ? dto.getFat() : 0.0)
                .date(dto.getDate())
                .build();
    }

    /* ── NutritionGoal ↔ NutritionGoalDTO ──────────────────── */
    public NutritionGoalDTO toDTO(NutritionGoal entity) {
        if (entity == null) return null;
        return NutritionGoalDTO.builder()
                .calorieGoal(entity.getCalorieGoal())
                .proteinGoal(entity.getProteinGoal())
                .carbsGoal(entity.getCarbsGoal())
                .fatGoal(entity.getFatGoal())
                .build();
    }

    public void updateGoalFromDTO(NutritionGoalDTO dto, NutritionGoal entity) {
        if (dto == null || entity == null) return;
        if (dto.getCalorieGoal() != null) entity.setCalorieGoal(dto.getCalorieGoal());
        if (dto.getProteinGoal() != null) entity.setProteinGoal(dto.getProteinGoal());
        if (dto.getCarbsGoal()   != null) entity.setCarbsGoal(dto.getCarbsGoal());
        if (dto.getFatGoal()     != null) entity.setFatGoal(dto.getFatGoal());
    }
}
