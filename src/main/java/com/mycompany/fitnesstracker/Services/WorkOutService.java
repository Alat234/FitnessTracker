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
        Set<Long> exerciseIds = dto.getExerciseSets().stream()
                .map(ExerciseSetDTO::getExerciseId)
                .collect(Collectors.toSet());

        Map<Long, Exercise> exerciseProxyMap = exerciseIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        exerciseRepository::getReferenceById
                ));


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
                .orElseThrow(() -> new RuntimeException("Тренування не знайдено"));

        if (!workOut.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Немає прав на видалення");
        }
        workOutRepository.delete(workOut);
    }
}