package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.ConnectionMapper;
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
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymServiceTest {

    @Mock
    private UserService          userService;
    @Mock
    private UserRepository       userRepository;
    @Mock
    private GymRepository        gymRepository;
    @Mock
    private GymTrainerRepository gymTrainerRepository;

    private GymService service;

    private User owner;   // id 1, gym owner
    private User admin;   // id 9, admin
    private User regular; // id 5, plain user
    private User trainer; // id 2, trainer

    @BeforeEach
    void setUp() {
        GymMapper gymMapper = new GymMapper(new ConnectionMapper());
        service = new GymService(userService, userRepository, gymRepository, gymTrainerRepository, gymMapper);

        owner   = user(1L, "owner@test.com",   Role.ROLE_GYM_OWNER);
        admin   = user(9L, "admin@test.com",   Role.ROLE_ADMIN);
        regular = user(5L, "user@test.com",    Role.ROLE_USER);
        trainer = user(2L, "trainer@test.com", Role.ROLE_TRAINER);
    }

    /* ── createGym ────────────────────────────────────────── */

    @Test
    void createGym_gymOwner_success() {
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.existsByGymOwner(owner)).thenReturn(false);
        when(gymRepository.save(any(Gym.class))).thenAnswer(inv -> inv.getArgument(0));

        GymDTO dto = service.createGym(request("Iron Gym"), "owner@test.com");

        assertThat(dto.getName()).isEqualTo("Iron Gym");
        assertThat(dto.getOwner().getEmail()).isEqualTo("owner@test.com");
        assertThat(dto.getTrainers()).isEmpty();
    }

    @Test
    void createGym_admin_success() {
        when(userService.getValidatedUserForAction("admin@test.com")).thenReturn(admin);
        when(gymRepository.existsByGymOwner(admin)).thenReturn(false);
        when(gymRepository.save(any(Gym.class))).thenAnswer(inv -> inv.getArgument(0));

        GymDTO dto = service.createGym(request("Admin Gym"), "admin@test.com");

        assertThat(dto.getName()).isEqualTo("Admin Gym");
        assertThat(dto.getOwner().getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    void createGym_roleUser_forbidden() {
        when(userService.getValidatedUserForAction("user@test.com")).thenReturn(regular);

        assertBase(() -> service.createGym(request("Iron Gym"), "user@test.com"),
                HttpStatus.FORBIDDEN, "Gym owner role required");
    }

    @Test
    void createGym_duplicate_conflict() {
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.existsByGymOwner(owner)).thenReturn(true);

        assertBase(() -> service.createGym(request("Iron Gym"), "owner@test.com"),
                HttpStatus.CONFLICT, "You already have a gym");
    }

    @Test
    void createGym_blankName_badRequest() {
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);

        assertBase(() -> service.createGym(request("   "), "owner@test.com"),
                HttpStatus.BAD_REQUEST, "Gym name is required");
    }

    /* ── getMyGyms ────────────────────────────────────────── */

    @Test
    void getMyGyms_noGym_empty() {
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findByGymOwner(owner)).thenReturn(Optional.empty());

        assertThat(service.getMyGyms("owner@test.com")).isEmpty();
    }

    @Test
    void getMyGyms_withGym_oneElement() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findByGymOwner(owner)).thenReturn(Optional.of(gym));
        when(gymTrainerRepository.findAllByGym(gym)).thenReturn(List.of());

        List<GymDTO> gyms = service.getMyGyms("owner@test.com");

        assertThat(gyms).hasSize(1);
        assertThat(gyms.get(0).getId()).isEqualTo(10L);
        assertThat(gyms.get(0).getOwner().getEmail()).isEqualTo("owner@test.com");
    }

    /* ── getGym ───────────────────────────────────────────── */

    @Test
    void getGym_returnsDtoWithTrainers() {
        Gym gym = gym(10L, owner);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(gymTrainerRepository.findAllByGym(gym)).thenReturn(List.of(gymTrainer(100L, gym, trainer)));

        GymDTO dto = service.getGym(10L);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTrainers()).hasSize(1);
        assertThat(dto.getTrainers().get(0).getTrainer().getEmail()).isEqualTo("trainer@test.com");
    }

    @Test
    void getGym_unknown_notFound() {
        when(gymRepository.findById(404L)).thenReturn(Optional.empty());

        assertBase(() -> service.getGym(404L),
                HttpStatus.NOT_FOUND, "Gym not found");
    }

    /* ── updateGym ────────────────────────────────────────── */

    @Test
    void updateGym_owner_success() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(gymRepository.save(any(Gym.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gymTrainerRepository.findAllByGym(gym)).thenReturn(List.of());

        GymDTO dto = service.updateGym(10L, request("New Name"), "owner@test.com");

        assertThat(dto.getName()).isEqualTo("New Name");
        assertThat(gym.getName()).isEqualTo("New Name");
    }

    @Test
    void updateGym_admin_success() {
        Gym gym = gym(10L, owner); // owned by someone else
        when(userService.getValidatedUserForAction("admin@test.com")).thenReturn(admin);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(gymRepository.save(any(Gym.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gymTrainerRepository.findAllByGym(gym)).thenReturn(List.of());

        GymDTO dto = service.updateGym(10L, request("Admin Edit"), "admin@test.com");

        assertThat(dto.getName()).isEqualTo("Admin Edit");
    }

    @Test
    void updateGym_nonOwner_forbidden() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("user@test.com")).thenReturn(regular);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));

        assertBase(() -> service.updateGym(10L, request("Hack"), "user@test.com"),
                HttpStatus.FORBIDDEN, "You do not own this gym");
    }

    @Test
    void updateGym_blankName_badRequest() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));

        assertBase(() -> service.updateGym(10L, request("  "), "owner@test.com"),
                HttpStatus.BAD_REQUEST, "Gym name is required");
    }

    /* ── addTrainer ───────────────────────────────────────── */

    @Test
    void addTrainer_owner_success() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(userRepository.findUserByEmailIs("trainer@test.com")).thenReturn(Optional.of(trainer));
        when(gymTrainerRepository.existsByGymAndTrainer(gym, trainer)).thenReturn(false);
        when(gymTrainerRepository.save(any(GymTrainer.class))).thenAnswer(inv -> inv.getArgument(0));

        GymTrainerDTO dto = service.addTrainer(10L, addTrainer("trainer@test.com"), "owner@test.com");

        assertThat(dto.getTrainer().getEmail()).isEqualTo("trainer@test.com");
        assertThat(dto.getTrainer().getId()).isEqualTo(2L);
    }

    @Test
    void addTrainer_admin_success() {
        Gym gym = gym(10L, owner); // owned by someone else
        when(userService.getValidatedUserForAction("admin@test.com")).thenReturn(admin);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(userRepository.findUserByEmailIs("trainer@test.com")).thenReturn(Optional.of(trainer));
        when(gymTrainerRepository.existsByGymAndTrainer(gym, trainer)).thenReturn(false);
        when(gymTrainerRepository.save(any(GymTrainer.class))).thenAnswer(inv -> inv.getArgument(0));

        GymTrainerDTO dto = service.addTrainer(10L, addTrainer("trainer@test.com"), "admin@test.com");

        assertThat(dto.getTrainer().getEmail()).isEqualTo("trainer@test.com");
    }

    @Test
    void addTrainer_unknownEmail_notFound() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(userRepository.findUserByEmailIs("ghost@test.com")).thenReturn(Optional.empty());

        assertBase(() -> service.addTrainer(10L, addTrainer("ghost@test.com"), "owner@test.com"),
                HttpStatus.NOT_FOUND, "User not found");
    }

    @Test
    void addTrainer_nonTrainer_badRequest() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(userRepository.findUserByEmailIs("user@test.com")).thenReturn(Optional.of(regular));

        assertBase(() -> service.addTrainer(10L, addTrainer("user@test.com"), "owner@test.com"),
                HttpStatus.BAD_REQUEST, "User is not a trainer");
    }

    @Test
    void addTrainer_duplicate_conflict() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(userRepository.findUserByEmailIs("trainer@test.com")).thenReturn(Optional.of(trainer));
        when(gymTrainerRepository.existsByGymAndTrainer(gym, trainer)).thenReturn(true);

        assertBase(() -> service.addTrainer(10L, addTrainer("trainer@test.com"), "owner@test.com"),
                HttpStatus.CONFLICT, "Trainer already in this gym");
    }

    @Test
    void addTrainer_nonOwner_forbidden() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("user@test.com")).thenReturn(regular);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));

        assertBase(() -> service.addTrainer(10L, addTrainer("trainer@test.com"), "user@test.com"),
                HttpStatus.FORBIDDEN, "You do not own this gym");
    }

    /* ── removeTrainer (trainerId = trainer User id) ──────── */

    @Test
    void removeTrainer_owner_success() {
        Gym gym = gym(10L, owner);
        GymTrainer link = gymTrainer(100L, gym, trainer);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(trainer));
        when(gymTrainerRepository.findByGymAndTrainer(gym, trainer)).thenReturn(Optional.of(link));

        service.removeTrainer(10L, 2L, "owner@test.com");

        verify(gymTrainerRepository).delete(link);
    }

    @Test
    void removeTrainer_admin_success() {
        Gym gym = gym(10L, owner); // owned by someone else
        GymTrainer link = gymTrainer(100L, gym, trainer);
        when(userService.getValidatedUserForAction("admin@test.com")).thenReturn(admin);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(trainer));
        when(gymTrainerRepository.findByGymAndTrainer(gym, trainer)).thenReturn(Optional.of(link));

        service.removeTrainer(10L, 2L, "admin@test.com");

        verify(gymTrainerRepository).delete(link);
    }

    @Test
    void removeTrainer_nonOwner_forbidden() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("user@test.com")).thenReturn(regular);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));

        assertBase(() -> service.removeTrainer(10L, 2L, "user@test.com"),
                HttpStatus.FORBIDDEN, "You do not own this gym");
    }

    @Test
    void removeTrainer_unknownGym_notFound() {
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findById(404L)).thenReturn(Optional.empty());

        assertBase(() -> service.removeTrainer(404L, 2L, "owner@test.com"),
                HttpStatus.NOT_FOUND, "Gym not found");
    }

    @Test
    void removeTrainer_missingLink_notFound() {
        Gym gym = gym(10L, owner);
        when(userService.getValidatedUserForAction("owner@test.com")).thenReturn(owner);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(trainer));
        when(gymTrainerRepository.findByGymAndTrainer(gym, trainer)).thenReturn(Optional.empty());

        assertBase(() -> service.removeTrainer(10L, 2L, "owner@test.com"),
                HttpStatus.NOT_FOUND, "Trainer not in this gym");
    }

    /* ── getTrainers ──────────────────────────────────────── */

    @Test
    void getTrainers_returnsList() {
        Gym gym = gym(10L, owner);
        when(gymRepository.findById(10L)).thenReturn(Optional.of(gym));
        when(gymTrainerRepository.findAllByGym(gym)).thenReturn(List.of(gymTrainer(100L, gym, trainer)));

        List<GymTrainerDTO> trainers = service.getTrainers(10L);

        assertThat(trainers).hasSize(1);
        assertThat(trainers.get(0).getTrainer().getEmail()).isEqualTo("trainer@test.com");
    }

    @Test
    void getTrainers_unknownGym_notFound() {
        when(gymRepository.findById(404L)).thenReturn(Optional.empty());

        assertBase(() -> service.getTrainers(404L),
                HttpStatus.NOT_FOUND, "Gym not found");
    }

    /* ── helpers ──────────────────────────────────────────── */

    private static User user(long id, String email, Role role) {
        return User.builder().id(id).email(email).role(role).build();
    }

    private static Gym gym(long id, User gymOwner) {
        Gym gym = new Gym();
        gym.setId(id);
        gym.setName("Iron Gym");
        gym.setGymOwner(gymOwner);
        return gym;
    }

    private static GymTrainer gymTrainer(long id, Gym gym, User trainer) {
        return GymTrainer.builder().id(id).gym(gym).trainer(trainer).build();
    }

    private static GymRequest request(String name) {
        return GymRequest.builder().name(name).description("desc").address("addr")
                .city("city").phoneNumber("+100").email("gym@test.com").build();
    }

    private static AddTrainerRequest addTrainer(String email) {
        return AddTrainerRequest.builder().email(email).build();
    }

    private static void assertBase(ThrowingCallable call, HttpStatus status, String message) {
        assertThatThrownBy(call)
                .isInstanceOf(BaseException.class)
                .satisfies(t -> {
                    BaseException e = (BaseException) t;
                    assertThat(e.getStatus()).isEqualTo(status);
                    assertThat(e.getMessage()).isEqualTo(message);
                });
    }
}
