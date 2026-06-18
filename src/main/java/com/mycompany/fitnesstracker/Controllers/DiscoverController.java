package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.Discover.DiscoverGymCardDTO;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverGymDetailsDTO;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverTrainerCardDTO;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverTrainerDetailsDTO;
import com.mycompany.fitnesstracker.Models.Discover.TrainerConnectResultDTO;
import com.mycompany.fitnesstracker.Models.UserDTO;
import com.mycompany.fitnesstracker.Services.DiscoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Authenticated-only Discover module. Public read of gyms + the current user's
 * gym selection. Conceptually separate from gym-owner CRUD (GymController).
 */
@RestController
@RequestMapping("/api/discover")
@RequiredArgsConstructor
public class DiscoverController {

    private final DiscoverService discoverService;

    @GetMapping("/gyms")
    public ResponseEntity<List<DiscoverGymCardDTO>> getGyms() {
        return ResponseEntity.ok(discoverService.getGyms());
    }

    @GetMapping("/gyms/{id}")
    public ResponseEntity<DiscoverGymDetailsDTO> getGym(@PathVariable Long id) {
        return ResponseEntity.ok(discoverService.getGym(id));
    }

    @PostMapping("/gyms/{id}/select")
    public ResponseEntity<UserDTO> selectGym(@PathVariable Long id) {
        return ResponseEntity.ok(discoverService.selectGym(id));
    }

    @DeleteMapping("/gyms/selection")
    public ResponseEntity<UserDTO> leaveGym() {
        return ResponseEntity.ok(discoverService.leaveGym());
    }

    /* ── Trainers ── */

    @GetMapping("/trainers")
    public ResponseEntity<List<DiscoverTrainerCardDTO>> getTrainers() {
        return ResponseEntity.ok(discoverService.getTrainers());
    }

    @GetMapping("/trainers/{id}")
    public ResponseEntity<DiscoverTrainerDetailsDTO> getTrainer(@PathVariable Long id) {
        return ResponseEntity.ok(discoverService.getTrainer(id));
    }

    @PostMapping("/trainers/{id}/connect")
    public ResponseEntity<TrainerConnectResultDTO> connectTrainer(@PathVariable Long id) {
        return ResponseEntity.ok(discoverService.connectTrainer(id));
    }
}
