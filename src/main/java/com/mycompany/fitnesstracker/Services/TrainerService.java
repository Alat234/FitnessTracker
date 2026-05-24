package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.WorkOutMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.Trainer.ClientDTO;
import com.mycompany.fitnesstracker.Models.Trainer.ClientProgressDTO;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.UserInfo;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOut;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOutDTO;
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
public class TrainerService {

    private final UserService        userService;
    private final UserRepository     userRepository;
    private final WorkOutRepository  workOutRepository;
    private final WorkOutMapper      workOutMapper;
    private final NutritionService   nutritionService;

    /* ── Permission guard ──────────────────────────────────── */
    private User requireTrainer(String email) {
        User user = userService.getValidatedUserForAction(email);
        if (user.getRole() != Role.ROLE_TRAINER
                && user.getRole() != Role.ROLE_ADMIN
                && user.getRole() != Role.ROLE_GYM_OWNER) {
            throw new BaseException("Trainer role required", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    /* ── List of clients ───────────────────────────────────── */
    @Transactional
    public List<ClientDTO> getClients(String trainerEmail) {
        User trainer = requireTrainer(trainerEmail);

        return userRepository.findAllClientsByTrainer(trainer).stream()
                .map(this::toClientDTO)
                .collect(Collectors.toList());
    }

    /* ── Full progress of a single client ──────────────────── */
    @Transactional
    public ClientProgressDTO getClientProgress(Long clientId, String trainerEmail) {
        User trainer = requireTrainer(trainerEmail);

        User client = userRepository.findUserById(clientId)
                .orElseThrow(() -> new BaseException("Client not found", HttpStatus.NOT_FOUND));

        UserInfo info = client.getUserInfo();
        if (info == null || info.getTrainer() == null
                || !info.getTrainer().getId().equals(trainer.getId())) {
            throw new BaseException("This client is not assigned to you", HttpStatus.FORBIDDEN);
        }

        List<WorkOut> history = workOutRepository.findAllByUserOrderByStartTimeDesc(client);

        double totalVolume = history.stream()
                .flatMap(w -> w.getExerciseSets().stream())
                .mapToDouble(s -> s.getWeight() * s.getReps())
                .sum();

        List<WorkOutDTO> historyDTO = history.stream()
                .map(workOutMapper::toDTO)
                .collect(Collectors.toList());

        return ClientProgressDTO.builder()
                .client(toClientDTO(client))
                .workoutHistory(historyDTO)
                .totalWorkouts(history.size())
                .totalVolumeKg(totalVolume)
                .todayNutrition(nutritionService.getDailySummary(LocalDate.now(), client.getEmail()))
                .build();
    }

    /* ── helpers ───────────────────────────────────────────── */
    private ClientDTO toClientDTO(User user) {
        UserInfo info = user.getUserInfo();
        int totalWorkouts = workOutRepository.findAllByUserOrderByStartTimeDesc(user).size();

        return ClientDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(info != null ? info.getFirstName() : null)
                .lastName(info != null ? info.getLastName() : null)
                .phoneNumber(info != null ? info.getPhoneNumber() : null)
                .totalWorkouts(totalWorkouts)
                .build();
    }
}
