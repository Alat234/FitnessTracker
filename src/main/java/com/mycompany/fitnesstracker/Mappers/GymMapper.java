package com.mycompany.fitnesstracker.Mappers;

import com.mycompany.fitnesstracker.Models.Gym;
import com.mycompany.fitnesstracker.Models.GymEntities.GymDTO;
import com.mycompany.fitnesstracker.Models.GymEntities.GymTrainer;
import com.mycompany.fitnesstracker.Models.GymEntities.GymTrainerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GymMapper {

    /* Перевикористовуємо публічну форму користувача (id/email/firstName/lastName). */
    private final ConnectionMapper connectionMapper;

    /* ── Gym → GymDTO ──────────────────────────────────────── */
    public GymDTO toDTO(Gym gym, List<GymTrainer> trainers) {
        if (gym == null) return null;
        List<GymTrainerDTO> trainerDTOs = (trainers == null ? Collections.<GymTrainer>emptyList() : trainers)
                .stream()
                .map(this::toTrainerDTO)
                .collect(Collectors.toList());

        return GymDTO.builder()
                .id(gym.getId())
                .name(gym.getName())
                .description(gym.getDescription())
                .address(gym.getAddress())
                .city(gym.getCity())
                .phoneNumber(gym.getPhoneNumber())
                .email(gym.getEmail())
                .owner(connectionMapper.toUserDTO(gym.getGymOwner()))
                .trainers(trainerDTOs)
                .createdAt(gym.getCreatedAt())
                .build();
    }

    public GymDTO toDTO(Gym gym) {
        return toDTO(gym, Collections.emptyList());
    }

    /* ── GymTrainer → GymTrainerDTO ────────────────────────── */
    public GymTrainerDTO toTrainerDTO(GymTrainer gymTrainer) {
        if (gymTrainer == null) return null;
        return GymTrainerDTO.builder()
                .id(gymTrainer.getId())
                .trainer(connectionMapper.toUserDTO(gymTrainer.getTrainer()))
                .build();
    }
}
