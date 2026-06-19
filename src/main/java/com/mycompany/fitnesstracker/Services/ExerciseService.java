package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.ExerciseMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.Exercise;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.ExerciseDTO;
import com.mycompany.fitnesstracker.Repositories.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    /**
     * Admin gate for write operations. Called BEFORE broad try/catch blocks so a
     * FORBIDDEN is not swallowed and converted into a 500. Service-layer check
     * because the project does not enable method security (mirrors ArticleService).
     */
    private void requireAdmin() {
        User user = userService.getUserByJWt();
        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new BaseException("Admin access required", HttpStatus.FORBIDDEN);
        }
    }

    public ExerciseDTO createExercise(ExerciseDTO exerciseDTO, MultipartFile file) {
        requireAdmin();
        try {
            String imageUrl = null;

            // 🔥 Якщо є файл - зберігаємо його на диск
            if (file != null && !file.isEmpty()) {
                imageUrl = fileStorageService.saveFile(file);
            }

            var exercise = Exercise.builder()
                    .name(exerciseDTO.getName())
                    .bodyPart(exerciseDTO.getBodyPart())
                    .description(exerciseDTO.getDescription())
                    .imageUrl(imageUrl) // Додаємо шлях до картинки
                    .build();

            Exercise savedExercise = exerciseRepository.save(exercise);
            return exerciseMapper.toDTO(savedExercise);

        } catch (DataIntegrityViolationException e) {
            log.error("Failed to create exercise. Name '{}' already exists.", exerciseDTO.getName());
            throw new BaseException("Exercise with this name already exists", HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Unexpected error while creating exercise '{}': {}", exerciseDTO.getName(), e.getMessage(), e);
            throw new BaseException("Internal server error while creating exercise", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<ExerciseDTO> getAllExercises() {
        try {
            List<Exercise> allExercises = exerciseRepository.findAll();
            log.info("Found {} exercises in the database", allExercises.size());
            return allExercises.stream()
                    .map(exerciseMapper::toDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Error while fetching exercises: {}", e.getMessage(), e);
            throw new BaseException("Failed to fetch exercises", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteExerciseById(Long exerciseId) {
        requireAdmin();
        try {
            log.info("Delete exercise with id: {}", exerciseId);
            exerciseRepository.deleteById(exerciseId);
        } catch (Exception e) {
            log.error("Failed to delete exercise with id: {}", exerciseId);
            throw new BaseException("Failed to delete exercise", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ExerciseDTO updateExercise(ExerciseDTO exerciseDTO, MultipartFile file) {
        requireAdmin();
        Exercise oldExercise = exerciseRepository.findById(exerciseDTO.getId())
                .orElseThrow(() -> new BaseException("Exercise not found", HttpStatus.NOT_FOUND));

        // Оновлюємо текстові дані через мапер
        exerciseMapper.updateEntityFromDto(exerciseDTO, oldExercise);

        // 🔥 Якщо прийшов новий файл - зберігаємо його і оновлюємо URL
        if (file != null && !file.isEmpty()) {
            String newImageUrl = fileStorageService.saveFile(file);
            oldExercise.setImageUrl(newImageUrl);
        }

        Exercise savedExercise = exerciseRepository.save(oldExercise);
        return exerciseMapper.toDTO(savedExercise);
    }
}
