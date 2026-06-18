package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.Nutrition.CalorieEstimateDTO;
import com.mycompany.fitnesstracker.Models.Nutrition.DailyNutritionSummaryDTO;
import com.mycompany.fitnesstracker.Models.Nutrition.NutritionGoalDTO;
import com.mycompany.fitnesstracker.Models.Nutrition.NutritionLogDTO;
import com.mycompany.fitnesstracker.Services.NutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;

    /* ── Meal logs ─────────────────────────────────────────── */

    /** Створити новий запис прийому їжі. */
    @PostMapping("/log")
    public ResponseEntity<NutritionLogDTO> addLog(@RequestBody NutritionLogDTO dto) {
        String email = currentUserEmail();
        NutritionLogDTO created = nutritionService.addLog(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Список записів за день (для відображення списку страв). */
    @GetMapping("/log")
    public ResponseEntity<List<NutritionLogDTO>> getLogs(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String email = currentUserEmail();
        return ResponseEntity.ok(nutritionService.getLogsForDay(date, email));
    }

    /** Видалити запис прийому їжі. */
    @DeleteMapping("/log/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        String email = currentUserEmail();
        nutritionService.deleteLog(id, email);
        return ResponseEntity.noContent().build();
    }

    /** Повний підсумок за день: список + сумарні макро + цілі. */
    @GetMapping("/summary")
    public ResponseEntity<DailyNutritionSummaryDTO> getDailySummary(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String email = currentUserEmail();
        return ResponseEntity.ok(nutritionService.getDailySummary(date, email));
    }

    /* ── Daily macro goals ─────────────────────────────────── */

    @GetMapping("/goals")
    public ResponseEntity<NutritionGoalDTO> getGoals() {
        String email = currentUserEmail();
        return ResponseEntity.ok(nutritionService.getGoals(email));
    }

    @PutMapping("/goals")
    public ResponseEntity<NutritionGoalDTO> updateGoals(@RequestBody NutritionGoalDTO dto) {
        String email = currentUserEmail();
        return ResponseEntity.ok(nutritionService.updateGoals(dto, email));
    }

    /* ── Calorie calculator ───────────────────────────────── */

    /** BMR/TDEE estimate + maintain/lose/gain suggestions from stored body metrics. */
    @GetMapping("/calculator")
    public ResponseEntity<CalorieEstimateDTO> getCalorieEstimate() {
        String email = currentUserEmail();
        return ResponseEntity.ok(nutritionService.getCalorieEstimate(email));
    }

    /* ── helpers ──────────────────────────────────────────── */
    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
