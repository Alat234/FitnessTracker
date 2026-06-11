package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.WorkOutMapper;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.*;
import com.mycompany.fitnesstracker.Repositories.ExerciseRepository;
import com.mycompany.fitnesstracker.Repositories.WorkOutRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOutService {
    private final WorkOutRepository workOutRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserService userService;
    private final WorkOutMapper workOutMapper;

    @Transactional
    public void saveWorkOut(WorkOutDTO dto, String email) {
        User user = userService.getValidatedUserForAction(email);

        if (dto.getExerciseSets() == null || dto.getExerciseSets().isEmpty()) {
            throw new RuntimeException("Workout must contain at least one set");
        }

        Set<Long> exerciseIds = dto.getExerciseSets().stream()
                .map(ExerciseSetDTO::getExerciseId)
                .collect(Collectors.toSet());

        /* Замість getReferenceById — реально витягуємо вправи з БД.
           Це дає одразу зрозумілу помилку, якщо якогось id не існує,
           замість невиразного "foreign key constraint fails" на INSERT. */
        List<Exercise> foundExercises = exerciseRepository.findAllById(exerciseIds);
        Map<Long, Exercise> exerciseProxyMap = foundExercises.stream()
                .collect(Collectors.toMap(Exercise::getId, e -> e));

        if (exerciseProxyMap.size() != exerciseIds.size()) {
            Set<Long> missing = new java.util.HashSet<>(exerciseIds);
            missing.removeAll(exerciseProxyMap.keySet());
            throw new RuntimeException(
                "Unknown exercise IDs: " + missing +
                ". Create these exercises first or use existing IDs."
            );
        }

        WorkOut workOut = workOutMapper.toEntity(dto, exerciseProxyMap);
        workOut.setUser(user);

        workOutRepository.save(workOut);
    }

    @Transactional
    public List<WorkOutDTO> getWorkOutHistory(String email) {
        User user = userService.getValidatedUserForAction(email);
        return workOutRepository.findAllByUserOrderByStartTimeDesc(user).stream()
                .map(workOutMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteWorkOut(Long id, String email) {
        User user = userService.getValidatedUserForAction(email);
        WorkOut workOut = workOutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        if (!workOut.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to delete this workout");
        }
        workOutRepository.delete(workOut);
    }
}