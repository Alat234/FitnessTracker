package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.GymEntities.AddTrainerRequest;
import com.mycompany.fitnesstracker.Models.GymEntities.GymDTO;
import com.mycompany.fitnesstracker.Models.GymEntities.GymRequest;
import com.mycompany.fitnesstracker.Models.GymEntities.GymTrainerDTO;
import com.mycompany.fitnesstracker.Services.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gyms")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    /** Створити зал для поточного користувача (власник залу або адмін). */
    @PostMapping
    public ResponseEntity<GymDTO> createGym(@RequestBody GymRequest request) {
        GymDTO created = gymService.createGym(request, currentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Мої зали (MVP: 0 або 1). */
    @GetMapping("/my")
    public ResponseEntity<List<GymDTO>> getMyGyms() {
        return ResponseEntity.ok(gymService.getMyGyms(currentUserEmail()));
    }

    /** Деталі залу з тренерами (будь-який автентифікований). */
    @GetMapping("/{id}")
    public ResponseEntity<GymDTO> getGym(@PathVariable Long id) {
        return ResponseEntity.ok(gymService.getGym(id));
    }

    /** Оновити зал (лише власник або адмін). */
    @PutMapping("/{id}")
    public ResponseEntity<GymDTO> updateGym(@PathVariable Long id, @RequestBody GymRequest request) {
        return ResponseEntity.ok(gymService.updateGym(id, request, currentUserEmail()));
    }

    /** Додати тренера до залу за email (лише власник або адмін). */
    @PostMapping("/{id}/trainers")
    public ResponseEntity<GymTrainerDTO> addTrainer(@PathVariable Long id, @RequestBody AddTrainerRequest request) {
        GymTrainerDTO added = gymService.addTrainer(id, request, currentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    /** Прибрати тренера із залу за trainer User id (лише власник або адмін). */
    @DeleteMapping("/{id}/trainers/{trainerId}")
    public ResponseEntity<Void> removeTrainer(@PathVariable Long id, @PathVariable Long trainerId) {
        gymService.removeTrainer(id, trainerId, currentUserEmail());
        return ResponseEntity.noContent().build();
    }

    /** Список тренерів залу (будь-який автентифікований). */
    @GetMapping("/{id}/trainers")
    public ResponseEntity<List<GymTrainerDTO>> getTrainers(@PathVariable Long id) {
        return ResponseEntity.ok(gymService.getTrainers(id));
    }

    /* ── helpers ──────────────────────────────────────────── */
    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
