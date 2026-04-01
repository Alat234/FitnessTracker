package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.WorkoutEntities.ExerciseDTO;
import com.mycompany.fitnesstracker.Services.ExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/exercise")
public class ExerciseController {
    private final ExerciseService exerciseService;
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity< ExerciseDTO> createExercise(@RequestBody ExerciseDTO exerciseDTO){
        log.info("REST request to create exercise: {}", exerciseDTO.getName());
        return ResponseEntity.ok(exerciseService.createExercise(exerciseDTO));
    }
    @GetMapping()
    public  ResponseEntity<List<ExerciseDTO>> getAllExercises(){
        log.info("REST request to get all exercises");
        return ResponseEntity.ok().body(exerciseService.getAllExercises());

    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id){
        log.info("REST request to delete exercise: {}", id);
        exerciseService.deleteExerciseById(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExerciseDTO> updateExercise(@RequestBody ExerciseDTO exerciseDTO){
        log.info("REST request to update exercise: {}", exerciseDTO);
        return ResponseEntity.ok().body(exerciseService.updateExercise(exerciseDTO));

    }

}
