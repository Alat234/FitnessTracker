package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.WorkOutProgramEntities.ProgramRequestDTO;
import com.mycompany.fitnesstracker.Services.WorkOutProgramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/workout/program")
public class WorkoutProgramController {

    private final WorkOutProgramService workoutProgramService;

    @PostMapping("/create")
    public ResponseEntity<ProgramRequestDTO> createProgram(@RequestBody ProgramRequestDTO programRequestDTO){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok().body(workoutProgramService.CreateWorkOutProgram(programRequestDTO,email));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProgramRequestDTO>> getAllPrograms() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(workoutProgramService.GetAllWorkOutPrograms(email));
    }

    @PutMapping("/update")
    public ResponseEntity<ProgramRequestDTO> updateProgram(@RequestBody ProgramRequestDTO programRequestDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(workoutProgramService.UpdateWorkOutProgram(programRequestDTO, email));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        workoutProgramService.DeleteWorkOutProgram(id, email);
        return ResponseEntity.ok().build();
    }
}