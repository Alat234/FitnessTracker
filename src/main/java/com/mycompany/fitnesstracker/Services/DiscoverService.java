package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.UserMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverGymCardDTO;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverGymDetailsDTO;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverTrainerSummaryDTO;
import com.mycompany.fitnesstracker.Models.Gym;
import com.mycompany.fitnesstracker.Models.GymEntities.GymTrainer;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.UserDTO;
import com.mycompany.fitnesstracker.Models.UserInfo;
import com.mycompany.fitnesstracker.Repositories.GymRepository;
import com.mycompany.fitnesstracker.Repositories.GymTrainerRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Read-only Discover directory for authenticated users plus the user's gym selection.
 * Kept separate from gym-owner CRUD (GymService). Returns whitelist DTOs only.
 */
@Service
@RequiredArgsConstructor
public class DiscoverService {

    private final GymRepository        gymRepository;
    private final GymTrainerRepository gymTrainerRepository;
    private final UserRepository       userRepository;
    private final UserService          userService;
    private final UserMapper           userMapper;

    @Transactional
    public List<DiscoverGymCardDTO> getGyms() {
        return gymRepository.findPublicGyms().stream()
                .map(this::toCard)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiscoverGymDetailsDTO getGym(Long id) {
        return toDetails(loadPublicGym(id));
    }

    @Transactional
    public UserDTO selectGym(Long id) {
        Gym gym = loadPublicGym(id);
        User user = userService.getUserByJWt();
        UserInfo info = getOrCreateInfo(user);
        info.setGym(gym);
        userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO leaveGym() {
        User user = userService.getUserByJWt();
        UserInfo info = user.getUserInfo();
        if (info != null && info.getGym() != null) {
            info.setGym(null);
            userRepository.save(user);
        }
        return userMapper.toDTO(user);
    }

    /* ── helpers ── */

    /** Loads a gym only if public; private (isPublic=false) is hidden as 404. Null = public. */
    private Gym loadPublicGym(Long id) {
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new BaseException("Gym not found", HttpStatus.NOT_FOUND));
        if (Boolean.FALSE.equals(gym.getIsPublic())) {
            throw new BaseException("Gym not found", HttpStatus.NOT_FOUND);
        }
        return gym;
    }

    private UserInfo getOrCreateInfo(User user) {
        UserInfo info = user.getUserInfo();
        if (info == null) {
            info = new UserInfo();
            info.setUserIdentity(user);
            user.setUserInfo(info);
        }
        return info;
    }

    private DiscoverGymCardDTO toCard(Gym gym) {
        return DiscoverGymCardDTO.builder()
                .id(gym.getId())
                .name(gym.getName())
                .city(gym.getCity())
                .address(gym.getAddress())
                .imageUrl(gym.getImageUrl())
                .trainerCount(gymTrainerRepository.countByGym(gym))
                .build();
    }

    private DiscoverGymDetailsDTO toDetails(Gym gym) {
        List<GymTrainer> links = gymTrainerRepository.findAllByGym(gym);
        List<DiscoverTrainerSummaryDTO> trainers = links.stream()
                .map(this::toTrainerSummary)
                .collect(Collectors.toList());

        return DiscoverGymDetailsDTO.builder()
                .id(gym.getId())
                .name(gym.getName())
                .description(gym.getDescription())
                .city(gym.getCity())
                .address(gym.getAddress())
                .phoneNumber(gym.getPhoneNumber())
                .email(gym.getEmail())
                .imageUrl(gym.getImageUrl())
                .trainerCount(links.size())
                .trainers(trainers)
                .build();
    }

    private DiscoverTrainerSummaryDTO toTrainerSummary(GymTrainer link) {
        UserInfo ui = link.getTrainer() != null ? link.getTrainer().getUserInfo() : null;
        return DiscoverTrainerSummaryDTO.builder()
                .firstName(ui != null ? ui.getFirstName() : null)
                .lastName(ui != null ? ui.getLastName() : null)
                .build();
    }
}
