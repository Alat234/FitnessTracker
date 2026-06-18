package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.BodyMetricsRequest;
import com.mycompany.fitnesstracker.Models.ChangePasswordRequest;
import com.mycompany.fitnesstracker.Models.UpdateProfileRequest;
import com.mycompany.fitnesstracker.Models.UpdateTrainerProfileRequest;
import com.mycompany.fitnesstracker.Models.UserDTO;
import com.mycompany.fitnesstracker.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController

public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUserByJWT(){
        return ResponseEntity.ok(userService.getUserDTOByJWT());
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/body-metrics")
    public ResponseEntity<UserDTO> updateBodyMetrics(@Valid @RequestBody BodyMetricsRequest request) {
        return ResponseEntity.ok(userService.updateBodyMetrics(request));
    }

    @PutMapping("/me/trainer-profile")
    public ResponseEntity<UserDTO> updateTrainerProfile(@Valid @RequestBody UpdateTrainerProfileRequest request) {
        return ResponseEntity.ok(userService.updateTrainerProfile(request));
    }

}
