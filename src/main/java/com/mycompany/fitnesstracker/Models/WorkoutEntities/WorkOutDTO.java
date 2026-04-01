package com.mycompany.fitnesstracker.Models.WorkoutEntities;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkOutDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<ExerciseSetDTO> exerciseSets;
}