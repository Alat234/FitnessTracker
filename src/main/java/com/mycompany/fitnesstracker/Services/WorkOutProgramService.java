package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.WorkoutProgramMapper;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.WorkOutProgramEntities.ProgramRequestDTO;
import com.mycompany.fitnesstracker.Models.WorkOutProgramEntities.WorkoutProgram;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.Exercise;
import com.mycompany.fitnesstracker.Repositories.ExerciseRepository;
import com.mycompany.fitnesstracker.Repositories.WorkOutProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOutProgramService {
    private final WorkOutProgramRepository workOutProgramRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserService userService;
    private final WorkoutProgramMapper workoutProgramMapper;

    @Transactional
    public ProgramRequestDTO CreateWorkOutProgram(ProgramRequestDTO WorkoutProgramDTO, String email) {
        User user = userService.getValidatedUserForAction(email);

        Set<Long> exercisesId = WorkoutProgramDTO.getWorkoutTemplates().stream()
                .flatMap(day -> day.getExercises().stream())
                .map(ProgramRequestDTO.ExerciseGroupDTO::getExerciseId)
                .collect(Collectors.toSet());

        Map<Long, Exercise> exercisesProxyMap = exercisesId.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        exerciseRepository::getReferenceById
                ));

        WorkoutProgram workoutProgram = workoutProgramMapper.ToEntity(WorkoutProgramDTO, exercisesProxyMap);
        workoutProgram.setUser(user);

        workOutProgramRepository.save(workoutProgram);
        return workoutProgramMapper.toDTO(workoutProgram);
    }

    @Transactional
    public ProgramRequestDTO UpdateWorkOutProgram(ProgramRequestDTO WorkoutProgramDTO, String email) {
        User user = userService.getValidatedUserForAction(email);

        WorkoutProgram existingProgram = workOutProgramRepository.findById(WorkoutProgramDTO.getId())
                .orElseThrow(() -> new RuntimeException("Program not found"));

        if (!existingProgram.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("U don`t have rights to edit this program");
        }

        Set<Long> exercisesId = WorkoutProgramDTO.getWorkoutTemplates().stream()
                .flatMap(day -> day.getExercises().stream())
                .map(ProgramRequestDTO.ExerciseGroupDTO::getExerciseId)
                .collect(Collectors.toSet());

        Map<Long, Exercise> exercisesProxyMap = exercisesId.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        exerciseRepository::getReferenceById
                ));

        workoutProgramMapper.updateProgramFromDto(WorkoutProgramDTO, existingProgram, exercisesProxyMap);

        workOutProgramRepository.save(existingProgram);
        return workoutProgramMapper.toDTO(existingProgram);
    }

    @Transactional
    public void DeleteWorkOutProgram(Long programId, String email) {
        User user = userService.getValidatedUserForAction(email);

        WorkoutProgram existingProgram = workOutProgramRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Програму не знайдено"));

        if (!existingProgram.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("У вас немає прав для видалення цієї програми");
        }

        workOutProgramRepository.delete(existingProgram);
    }

    @Transactional(readOnly = true)
    public List<ProgramRequestDTO> GetAllWorkOutPrograms(String email) {

        User user = userService.getValidatedUserForAction(email);
        List<WorkoutProgram> programs = workOutProgramRepository.findAllByUser(user);
        return programs.stream()
                .map(workoutProgramMapper::toDTO)
                .collect(Collectors.toList());
    }
}