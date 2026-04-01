package com.mycompany.fitnesstracker.Mappers;

import com.mycompany.fitnesstracker.Models.WorkoutEntities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WorkOutMapper {

    public WorkOut toEntity(WorkOutDTO dto, Map<Long, Exercise> exerciseProxyMap) {
        WorkOut workOut = new WorkOut();
        workOut.setStartTime(dto.getStartTime());
        workOut.setEndTime(dto.getEndTime());

        dto.getExerciseSets().stream()
                .map(setDto -> {
                    ExerciseSet set = new ExerciseSet();
                    set.setReps(setDto.getReps());
                    set.setWeight(setDto.getWeight());
                    set.setExercise(exerciseProxyMap.get(setDto.getExerciseId()));
                    return set;
                })
                .forEach(workOut::addExerciseSet);

        return workOut;
    }

    public WorkOutDTO toDTO(WorkOut entity) {
        WorkOutDTO dto = new WorkOutDTO();
        dto.setId(entity.getId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        if (entity.getExerciseSets() != null) {
            List<ExerciseSetDTO> setDTOs = entity.getExerciseSets().stream()
                    .map(set -> {
                        ExerciseSetDTO setDto = new ExerciseSetDTO();
                        setDto.setReps(set.getReps());
                        setDto.setWeight(set.getWeight());

                        if (set.getExercise() != null) {
                            setDto.setExerciseId(set.getExercise().getId());
                        }
                        return setDto;
                    })
                    .collect(Collectors.toList());

            dto.setExerciseSets(setDTOs);
        }

        return dto;
    }
}