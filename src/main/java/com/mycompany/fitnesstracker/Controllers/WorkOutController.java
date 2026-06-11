package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOutDTO;
import com.mycompany.fitnesstracker.Services.WorkOutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@RestController
    @RequestMapping("/api/workout")
    @RequiredArgsConstructor
    public class WorkOutController {

        private final WorkOutService workOutService;

        @PostMapping("/save")
        public ResponseEntity<String> saveWorkout(@RequestBody WorkOutDTO workOutDTO) {
            try {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                workOutService.saveWorkOut(workOutDTO, email);
                return ResponseEntity.ok("Workout saved successfully");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Failed to save workout: " + e.getMessage());
            }
        }


        @GetMapping("/history")
        public ResponseEntity<List<WorkOutDTO>> getHistory() {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<WorkOutDTO> history = workOutService.getWorkOutHistory(email);
            return ResponseEntity.ok(history);
        }

        // Delete workout by ID
        @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteWorkout(@PathVariable Long id) {
            try {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                workOutService.deleteWorkOut(id, email);
                return ResponseEntity.ok("Workout deleted");
            } catch (Exception e) {
                return ResponseEntity.status(403).body("Failed to delete workout: " + e.getMessage());
            }
        }
    }

