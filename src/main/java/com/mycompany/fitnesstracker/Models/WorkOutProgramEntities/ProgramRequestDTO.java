package com.mycompany.fitnesstracker.Models.WorkOutProgramEntities;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class ProgramRequestDTO {
    private Long id;
    private String name;
    private String goal;
    private int dayAWeek;
    private List<DayRequestDTO> workoutTemplates;

    @Data
    @Builder
    public static class DayRequestDTO {
        private Long id;
        private String name;
        private List<ExerciseGroupDTO> exercises;
    }

    @Data
    @Builder
    public static class ExerciseGroupDTO {
        private Long exerciseId;
        private List<SetRequestDTO> sets;
    }

    @Data
    @Builder
    public static class SetRequestDTO {
        private Long id;
        private int reps;
        private double weight;
    }
}