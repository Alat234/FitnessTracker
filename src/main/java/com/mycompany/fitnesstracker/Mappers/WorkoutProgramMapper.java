package com.mycompany.fitnesstracker.Mappers;

import com.mycompany.fitnesstracker.Models.WorkOutProgramEntities.ProgramRequestDTO;
import com.mycompany.fitnesstracker.Models.WorkOutProgramEntities.SetTemplate;
import com.mycompany.fitnesstracker.Models.WorkOutProgramEntities.WorkoutProgram;
import com.mycompany.fitnesstracker.Models.WorkOutProgramEntities.WorkoutTemplate;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.Exercise;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Component
public class WorkoutProgramMapper {

    public ProgramRequestDTO toDTO(WorkoutProgram entity) {
        if (entity == null) return null;

        return ProgramRequestDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .goal(entity.getGoal())
                .dayAWeek(entity.getDayAWeek())
                .workoutTemplates(templatesToDtoList(entity.getWorkoutTemplates()))
                .build();
    }

    private List<ProgramRequestDTO.DayRequestDTO> templatesToDtoList(List<WorkoutTemplate> entities) {
        if (entities == null) return List.of();

        return entities.stream()
                .map(this::templateToDto)
                .collect(Collectors.toList());
    }

    private ProgramRequestDTO.DayRequestDTO templateToDto(WorkoutTemplate entity) {
        return ProgramRequestDTO.DayRequestDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .exercises(groupSetsByExercise(entity.getSetTemplates()))
                .build();
    }
    private List<ProgramRequestDTO.ExerciseGroupDTO> groupSetsByExercise(List<SetTemplate> sets) {
        if (sets == null) return List.of();


        Map<Long, List<SetTemplate>> groupedByExercise = sets.stream()
                .collect(Collectors.groupingBy(set -> set.getExercise().getId()));

        return groupedByExercise.entrySet().stream()
                .map(entry -> ProgramRequestDTO.ExerciseGroupDTO.builder()
                        .exerciseId(entry.getKey())
                        .sets(entry.getValue().stream()
                                .map(set -> ProgramRequestDTO.SetRequestDTO.builder()
                                        .id(set.getId())
                                        .reps(set.getReps())
                                        .weight(set.getWeight())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    public void updateProgramFromDto(ProgramRequestDTO dto, WorkoutProgram entity, Map<Long, Exercise> exercises) {
        if (dto == null || entity == null) return;

        entity.setName(dto.getName());
        entity.setGoal(dto.getGoal());
        entity.setDayAWeek(dto.getDayAWeek());
        entity.getWorkoutTemplates().clear();
        List<WorkoutTemplate> newTemplates = workoutTemplatesToEntity(
                dto.getWorkoutTemplates(), exercises, entity);
        entity.getWorkoutTemplates().addAll(newTemplates);
    }

    public WorkoutProgram ToEntity(ProgramRequestDTO dto, Map<Long, Exercise> exercises) {
        WorkoutProgram workoutProgram = WorkoutProgram.builder()
                .name(dto.getName())
                .dayAWeek(dto.getDayAWeek())
                .goal(dto.getGoal())
                .build();

        List<WorkoutTemplate> templates = workoutTemplatesToEntity(
                dto.getWorkoutTemplates(), exercises, workoutProgram);

        workoutProgram.setWorkoutTemplates(templates);
        return workoutProgram;
    }

    private List<WorkoutTemplate> workoutTemplatesToEntity(List<ProgramRequestDTO.DayRequestDTO> dtos, Map<Long, Exercise> exercises, WorkoutProgram workoutProgram) {

        return dtos.stream()
                .map(dayDto -> workoutTemplateToEntity(dayDto, exercises, workoutProgram))
                .collect(Collectors.toList());
    }

    private WorkoutTemplate workoutTemplateToEntity(ProgramRequestDTO.DayRequestDTO dto, Map<Long, Exercise> exercises, WorkoutProgram workoutProgram) {
        WorkoutTemplate workoutTemplate = WorkoutTemplate.builder()
                .name(dto.getName())
                .workoutProgram(workoutProgram)
                .build();

        List<SetTemplate> setTemplates = setTemplatesToEntity(
                dto.getExercises(), exercises, workoutTemplate);

        workoutTemplate.setSetTemplates(setTemplates);
        return workoutTemplate;
    }

    private List<SetTemplate> setTemplatesToEntity(List<ProgramRequestDTO.ExerciseGroupDTO> groups, Map<Long, Exercise> exercises, WorkoutTemplate workoutTemplate) {
        return groups.stream()
                .flatMap(group -> group.getSets().stream()
                        .map(setDto -> SetTemplate.builder()
                                .reps(setDto.getReps())
                                .weight(setDto.getWeight())
                                .exercise(exercises.get(group.getExerciseId()))
                                .workoutTemplate(workoutTemplate)
                                .build()
                        )
                )
                .collect(Collectors.toList());
    }
}