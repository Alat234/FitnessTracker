package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.NutritionMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Nutrition.*;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Repositories.NutritionGoalRepository;
import com.mycompany.fitnesstracker.Repositories.NutritionLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionService {

    private final NutritionLogRepository  logRepository;
    private final NutritionGoalRepository goalRepository;
    private final UserService             userService;
    private final NutritionMapper         nutritionMapper;

    /* ═══════════════════════════════════════════════
       MEAL LOGS
    ═══════════════════════════════════════════════ */

    @Transactional
    public NutritionLogDTO addLog(NutritionLogDTO dto, String email) {
        if (dto.getFoodName() == null || dto.getFoodName().isBlank()) {
            throw new BaseException("Food name is required", HttpStatus.BAD_REQUEST);
        }
        if (dto.getCalories() == null || dto.getCalories() < 0) {
            throw new BaseException("Calories must be a non-negative number", HttpStatus.BAD_REQUEST);
        }

        User user = userService.getValidatedUserForAction(email);

        NutritionLog entity = nutritionMapper.toEntity(dto);
        entity.setUser(user);
        if (entity.getDate() == null) entity.setDate(LocalDate.now());

        NutritionLog saved = logRepository.save(entity);
        return nutritionMapper.toDTO(saved);
    }

    @Transactional
    public List<NutritionLogDTO> getLogsForDay(LocalDate date, String email) {
        User user = userService.getValidatedUserForAction(email);
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();

        return logRepository
                .findAllByUserAndDateOrderByCreatedAtAsc(user, effectiveDate)
                .stream()
                .map(nutritionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DailyNutritionSummaryDTO getDailySummary(LocalDate date, String email) {
        User user = userService.getValidatedUserForAction(email);
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();

        List<NutritionLog> logs = logRepository
                .findAllByUserAndDateOrderByCreatedAtAsc(user, effectiveDate);

        double totalCalories = logs.stream().mapToDouble(l -> nz(l.getCalories())).sum();
        double totalProtein  = logs.stream().mapToDouble(l -> nz(l.getProtein())).sum();
        double totalCarbs    = logs.stream().mapToDouble(l -> nz(l.getCarbohydrates())).sum();
        double totalFat      = logs.stream().mapToDouble(l -> nz(l.getFat())).sum();

        return DailyNutritionSummaryDTO.builder()
                .date(effectiveDate)
                .logs(logs.stream().map(nutritionMapper::toDTO).collect(Collectors.toList()))
                .totalCalories(totalCalories)
                .totalProtein(totalProtein)
                .totalCarbohydrates(totalCarbs)
                .totalFat(totalFat)
                .goals(nutritionMapper.toDTO(getOrCreateGoal(user)))
                .build();
    }

    @Transactional
    public void deleteLog(Long id, String email) {
        User user = userService.getValidatedUserForAction(email);

        NutritionLog log = logRepository.findById(id)
                .orElseThrow(() -> new BaseException("Nutrition log not found", HttpStatus.NOT_FOUND));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new BaseException("You can't delete another user's log", HttpStatus.FORBIDDEN);
        }
        logRepository.delete(log);
    }

    /* ═══════════════════════════════════════════════
       DAILY GOALS
    ═══════════════════════════════════════════════ */

    @Transactional
    public NutritionGoalDTO getGoals(String email) {
        User user = userService.getValidatedUserForAction(email);
        return nutritionMapper.toDTO(getOrCreateGoal(user));
    }

    @Transactional
    public NutritionGoalDTO updateGoals(NutritionGoalDTO dto, String email) {
        if (dto.getCalorieGoal() != null && dto.getCalorieGoal() < 0
                || dto.getProteinGoal() != null && dto.getProteinGoal() < 0
                || dto.getCarbsGoal()   != null && dto.getCarbsGoal()   < 0
                || dto.getFatGoal()     != null && dto.getFatGoal()     < 0) {
            throw new BaseException("Goal values must be non-negative", HttpStatus.BAD_REQUEST);
        }

        User user = userService.getValidatedUserForAction(email);
        NutritionGoal goal = getOrCreateGoal(user);

        nutritionMapper.updateGoalFromDTO(dto, goal);
        return nutritionMapper.toDTO(goalRepository.save(goal));
    }

    /* Lazily create defaults for users that haven't set goals yet */
    private NutritionGoal getOrCreateGoal(User user) {
        return goalRepository.findByUser(user)
                .orElseGet(() -> goalRepository.save(NutritionGoal.defaultsFor(user)));
    }

    private double nz(Double v) { return v == null ? 0.0 : v; }
}
