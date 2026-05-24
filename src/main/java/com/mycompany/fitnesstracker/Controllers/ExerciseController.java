package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.Enums.BodyPart;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.ExerciseDTO;
import com.mycompany.fitnesstracker.Services.ExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/exercise")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExerciseDTO> createExercise(
            @RequestParam("name") String name,
            @RequestParam("bodyPart") BodyPart bodyPart,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        log.info("REST request to create exercise: {}", name);

        ExerciseDTO exerciseDTO = ExerciseDTO.builder()
                .name(name)
                .bodyPart(bodyPart)
                .description(description)
                .build();

        return ResponseEntity.ok(exerciseService.createExercise(exerciseDTO, file));
    }

    @GetMapping()
    public ResponseEntity<List<ExerciseDTO>> getAllExercises() {
        log.info("REST request to get all exercises");
        return ResponseEntity.ok().body(exerciseService.getAllExercises());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id) {
        log.info("REST request to delete exercise: {}", id);
        exerciseService.deleteExerciseById(id);
        return ResponseEntity.noContent().build();
    }

    // 🔥 ОНОВЛЕНО: Приймаємо FormData
    @PutMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExerciseDTO> updateExercise(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("bodyPart") BodyPart bodyPart,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        log.info("REST request to update exercise: {}", name);

        ExerciseDTO exerciseDTO = ExerciseDTO.builder()
                .id(id)
                .name(name)
                .bodyPart(bodyPart)
                .description(description)
                .build();

        return ResponseEntity.ok().body(exerciseService.updateExercise(exerciseDTO, file));
    }
}