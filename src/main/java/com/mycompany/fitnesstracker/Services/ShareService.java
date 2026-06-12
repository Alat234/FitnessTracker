package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.ConnectionMapper;
import com.mycompany.fitnesstracker.Mappers.WorkOutMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Connection.PermissionsDTO;
import com.mycompany.fitnesstracker.Models.Connection.SharedProgressDTO;
import com.mycompany.fitnesstracker.Models.Connection.UserConnection;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionStatus;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOut;
import com.mycompany.fitnesstracker.Repositories.UserConnectionRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import com.mycompany.fitnesstracker.Repositories.WorkOutRepository;
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
public class ShareService {

    private final UserConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final WorkOutRepository workOutRepository;
    private final WorkOutMapper workOutMapper;
    private final NutritionService nutritionService;
    private final ConnectionMapper connectionMapper;

    /**
     * Прогрес власника (owner) для viewer-а з ACCEPTED-зв'язком.
     * Права з усіх ACCEPTED-рядків пари (TRAINER + FRIEND) об'єднуються через OR.
     * Розділи без права — null; блок permissions присутній завжди.
     *
     * Не read-only транзакція: getDailySummary ліниво створює NutritionGoal,
     * якщо у власника його ще немає.
     */
    @Transactional
    public SharedProgressDTO getSharedProgress(Long ownerId, String viewerEmail) {
        User viewer = userService.getValidatedUserForAction(viewerEmail);

        User owner = userRepository.findUserById(ownerId)
                .orElseThrow(() -> new BaseException("User not found", HttpStatus.NOT_FOUND));

        List<UserConnection> accepted = connectionRepository
                .findAllByOwnerIdAndViewerAndStatus(ownerId, viewer, ConnectionStatus.ACCEPTED);
        if (accepted.isEmpty()) {
            throw new BaseException("No access to this user's progress", HttpStatus.FORBIDDEN);
        }

        boolean canViewWorkouts        = accepted.stream().anyMatch(UserConnection::isCanViewWorkouts);
        boolean canViewNutrition       = accepted.stream().anyMatch(UserConnection::isCanViewNutrition);
        boolean canViewBodyMetrics     = accepted.stream().anyMatch(UserConnection::isCanViewBodyMetrics);
        boolean canViewProgressSummary = accepted.stream().anyMatch(UserConnection::isCanViewProgressSummary);

        SharedProgressDTO.SharedProgressDTOBuilder builder = SharedProgressDTO.builder()
                .owner(connectionMapper.toUserDTO(owner))
                .permissions(PermissionsDTO.builder()
                        .workouts(canViewWorkouts)
                        .nutrition(canViewNutrition)
                        .bodyMetrics(canViewBodyMetrics)
                        .progressSummary(canViewProgressSummary)
                        .build());

        if (canViewWorkouts || canViewProgressSummary) {
            List<WorkOut> history = workOutRepository.findAllByUserOrderByStartTimeDesc(owner);

            if (canViewWorkouts) {
                builder.workoutHistory(history.stream()
                        .map(workOutMapper::toDTO)
                        .collect(Collectors.toList()));
            }
            if (canViewProgressSummary) {
                double totalVolume = history.stream()
                        .flatMap(w -> w.getExerciseSets().stream())
                        .mapToDouble(s -> s.getWeight() * s.getReps())
                        .sum();
                builder.totalWorkouts(history.size());
                builder.totalVolumeKg(totalVolume);
            }
        }

        if (canViewNutrition) {
            builder.todayNutrition(nutritionService.getDailySummary(LocalDate.now(), owner.getEmail()));
        }

        /* bodyMetrics: дані ще не реалізовані — залишається null */
        return builder.build();
    }
}
