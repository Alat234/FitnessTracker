package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.GymMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.Gym;
import com.mycompany.fitnesstracker.Models.GymEntities.AddTrainerRequest;
import com.mycompany.fitnesstracker.Models.GymEntities.GymDTO;
import com.mycompany.fitnesstracker.Models.GymEntities.GymRequest;
import com.mycompany.fitnesstracker.Models.GymEntities.GymTrainer;
import com.mycompany.fitnesstracker.Models.GymEntities.GymTrainerDTO;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Repositories.GymRepository;
import com.mycompany.fitnesstracker.Repositories.GymTrainerRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GymService {

    private final UserService           userService;
    private final UserRepository        userRepository;
    private final GymRepository         gymRepository;
    private final GymTrainerRepository  gymTrainerRepository;
    private final GymMapper             gymMapper;

    /* ── Create ────────────────────────────────────────────── */
    @Transactional
    public GymDTO createGym(GymRequest request, String email) {
        User owner = resolveUser(email);
        requireOwnerRole(owner);
        requireName(request);

        if (gymRepository.existsByGymOwner(owner)) {
            throw new BaseException("You already have a gym", HttpStatus.CONFLICT);
        }

        Gym gym = new Gym();
        applyRequest(gym, request);
        gym.setGymOwner(owner);

        Gym saved = gymRepository.save(gym);
        return gymMapper.toDTO(saved, Collections.emptyList());
    }

    /* ── My gyms (0 or 1) ──────────────────────────────────── */
    @Transactional
    public List<GymDTO> getMyGyms(String email) {
        User owner = resolveUser(email);
        return gymRepository.findByGymOwner(owner)
                .map(gym -> List.of(gymMapper.toDTO(gym, gymTrainerRepository.findAllByGym(gym))))
                .orElseGet(Collections::emptyList);
    }

    /* ── Single gym (any authenticated user) ───────────────── */
    @Transactional
    public GymDTO getGym(Long id) {
        Gym gym = loadGym(id);
        return gymMapper.toDTO(gym, gymTrainerRepository.findAllByGym(gym));
    }

    /* ── Update ────────────────────────────────────────────── */
    @Transactional
    public GymDTO updateGym(Long id, GymRequest request, String email) {
        User user = resolveUser(email);
        Gym gym = loadGym(id);
        requireOwnerOrAdmin(user, gym);
        requireName(request);

        applyRequest(gym, request);
        Gym saved = gymRepository.save(gym);
        return gymMapper.toDTO(saved, gymTrainerRepository.findAllByGym(saved));
    }

    /* ── Add trainer by email ──────────────────────────────── */
    @Transactional
    public GymTrainerDTO addTrainer(Long gymId, AddTrainerRequest request, String email) {
        User user = resolveUser(email);
        Gym gym = loadGym(gymId);
        requireOwnerOrAdmin(user, gym);

        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BaseException("Trainer email is required", HttpStatus.BAD_REQUEST);
        }

        User trainer = userRepository.findUserByEmailIs(request.getEmail())
                .orElseThrow(() -> new BaseException("User not found", HttpStatus.NOT_FOUND));

        if (trainer.getRole() != Role.ROLE_TRAINER) {
            throw new BaseException("User is not a trainer", HttpStatus.BAD_REQUEST);
        }

        if (gymTrainerRepository.existsByGymAndTrainer(gym, trainer)) {
            throw new BaseException("Trainer already in this gym", HttpStatus.CONFLICT);
        }

        GymTrainer link = GymTrainer.builder()
                .gym(gym)
                .trainer(trainer)
                .build();

        return gymMapper.toTrainerDTO(gymTrainerRepository.save(link));
    }

    /* ── Remove trainer (trainerId = trainer User id) ──────── */
    @Transactional
    public void removeTrainer(Long gymId, Long trainerId, String email) {
        User user = resolveUser(email);
        Gym gym = loadGym(gymId);
        requireOwnerOrAdmin(user, gym);

        User trainer = userRepository.findUserById(trainerId)
                .orElseThrow(() -> new BaseException("User not found", HttpStatus.NOT_FOUND));

        GymTrainer link = gymTrainerRepository.findByGymAndTrainer(gym, trainer)
                .orElseThrow(() -> new BaseException("Trainer not in this gym", HttpStatus.NOT_FOUND));

        gymTrainerRepository.delete(link);
    }

    /* ── List trainers (any authenticated user) ────────────── */
    @Transactional
    public List<GymTrainerDTO> getTrainers(Long gymId) {
        Gym gym = loadGym(gymId);
        return gymTrainerRepository.findAllByGym(gym).stream()
                .map(gymMapper::toTrainerDTO)
                .collect(Collectors.toList());
    }

    /* ── helpers ───────────────────────────────────────────── */
    private User resolveUser(String email) {
        return userService.getValidatedUserForAction(email);
    }

    private Gym loadGym(Long id) {
        return gymRepository.findById(id)
                .orElseThrow(() -> new BaseException("Gym not found", HttpStatus.NOT_FOUND));
    }

    private void requireOwnerRole(User user) {
        if (user.getRole() != Role.ROLE_GYM_OWNER && user.getRole() != Role.ROLE_ADMIN) {
            throw new BaseException("Gym owner role required", HttpStatus.FORBIDDEN);
        }
    }

    private void requireOwnerOrAdmin(User user, Gym gym) {
        boolean isOwner = gym.getGymOwner() != null
                && gym.getGymOwner().getId() != null
                && gym.getGymOwner().getId().equals(user.getId());
        if (!isOwner && user.getRole() != Role.ROLE_ADMIN) {
            throw new BaseException("You do not own this gym", HttpStatus.FORBIDDEN);
        }
    }

    private void requireName(GymRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new BaseException("Gym name is required", HttpStatus.BAD_REQUEST);
        }
    }

    private void applyRequest(Gym gym, GymRequest request) {
        gym.setName(request.getName());
        gym.setDescription(request.getDescription());
        gym.setAddress(request.getAddress());
        gym.setCity(request.getCity());
        gym.setPhoneNumber(request.getPhoneNumber());
        gym.setEmail(request.getEmail());
    }
}
