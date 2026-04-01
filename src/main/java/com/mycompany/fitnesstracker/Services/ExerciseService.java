package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.ExerciseMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.Exercise;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.ExerciseDTO;
import com.mycompany.fitnesstracker.Repositories.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    public ExerciseDTO createExercise(ExerciseDTO exerciseDTO) {
        try {
            var exercise = Exercise.builder()
                    .name(exerciseDTO.getName())
                    .bodyPart(exerciseDTO.getBodyPart())
                    .description(exerciseDTO.getDescription())
                    .build();
           Exercise savedExercise= exerciseRepository.save(exercise);
            return exerciseMapper.toDTO(savedExercise);



        }
        catch (DataIntegrityViolationException e) {
            log.error("Failed to create exercise. Name '{}' already exists.", exerciseDTO.getName());
            throw new BaseException("Exercise with this name already exists", HttpStatus.CONFLICT);

        } catch (Exception e) {
            log.error("Unexpected error while creating exercise '{}': {}", exerciseDTO.getName(), e.getMessage(), e);
            throw new BaseException("Internal server error while creating exercise", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    public List<ExerciseDTO> getAllExercises() {
        try {
            List<Exercise>allExercises= exerciseRepository.findAll();
            log.info("Found {} exercises in the database", allExercises.size());
            return allExercises.stream()
                    .map(exerciseMapper::toDTO)
                    .toList();
        }
        catch (Exception e) {
            log.error("Error while fetching exercises: {}", e.getMessage(), e);
            throw new BaseException("Failed to fetch exercises", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    public void deleteExerciseById(Long exerciseId) {
        try {
            log.info("Delete exercise with id: {}", exerciseId);
            exerciseRepository.deleteById(exerciseId);
        }
        catch (Exception e) {
            log.error("Failed to delete exercise with id: {}", exerciseId);
            throw  new BaseException("Failed to delete exercise", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public ExerciseDTO updateExercise(ExerciseDTO exerciseDTO) {
        Exercise oldExercise = exerciseRepository.findById(exerciseDTO.getId())
                .orElseThrow(() -> new BaseException("Exercise not found", HttpStatus.NOT_FOUND));
        exerciseMapper.updateEntityFromDto(exerciseDTO, oldExercise);
        Exercise savedExercise = exerciseRepository.save(oldExercise);
        return exerciseMapper.toDTO(savedExercise);
    }

}
