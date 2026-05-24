package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.Trainer.ClientDTO;
import com.mycompany.fitnesstracker.Models.Trainer.ClientProgressDTO;
import com.mycompany.fitnesstracker.Services.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    /** Список усіх клієнтів поточного тренера. */
    @GetMapping("/clients")
    public ResponseEntity<List<ClientDTO>> getClients() {
        String email = currentUserEmail();
        return ResponseEntity.ok(trainerService.getClients(email));
    }

    /** Деталі і прогрес конкретного клієнта. */
    @GetMapping("/clients/{id}/progress")
    public ResponseEntity<ClientProgressDTO> getClientProgress(@PathVariable Long id) {
        String email = currentUserEmail();
        return ResponseEntity.ok(trainerService.getClientProgress(id, email));
    }

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
