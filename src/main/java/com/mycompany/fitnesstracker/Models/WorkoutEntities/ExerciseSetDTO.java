package com.mycompany.fitnesstracker.Models.WorkoutEntities;

import lombok.Data;

@Data
public class ExerciseSetDTO {
    private Long exerciseId;
    private int reps;
    private double weight;
}
