package com.mycompany.fitnesstracker.Mappers;

import com.mycompany.fitnesstracker.Models.WorkoutEntities.Exercise;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.ExerciseDTO;
import org.springframework.stereotype.Component;

@Component
public class ExerciseMapper {

    public ExerciseDTO toDTO(Exercise exercise) {
        if (exercise == null) {
            return null;
        }

        ExerciseDTO dto = new ExerciseDTO();
        dto.setId(exercise.getId());
        dto.setName(exercise.getName());
        dto.setBodyPart(exercise.getBodyPart());
        dto.setDescription(exercise.getDescription());

        dto.setImageUrl(exercise.getImageUrl());

        return dto;
    }

    public void updateEntityFromDto(ExerciseDTO dto, Exercise entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getBodyPart() != null) {
            entity.setBodyPart(dto.getBodyPart());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        // Зазвичай файл оновлюється в Сервісі, але якщо передали готовий URL - мапимо і його
        if (dto.getImageUrl() != null) {
            entity.setImageUrl(dto.getImageUrl());
        }
    }
}