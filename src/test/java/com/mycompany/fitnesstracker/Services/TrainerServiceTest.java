package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.WorkOutMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Connection.UserConnection;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionType;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.Nutrition.DailyNutritionSummaryDTO;
import com.mycompany.fitnesstracker.Models.Trainer.ClientDTO;
import com.mycompany.fitnesstracker.Models.Trainer.ClientProgressDTO;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.UserInfo;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.ExerciseSet;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOut;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOutDTO;
import com.mycompany.fitnesstracker.Repositories.UserConnectionRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import com.mycompany.fitnesstracker.Repositories.WorkOutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserConnectionRepository connectionRepository;
    @Mock
    private WorkOutRepository workOutRepository;
    @Mock
    private WorkOutMapper workOutMapper;
    @Mock
    private NutritionService nutritionService;

    private TrainerService service;

    private User trainer;
    private User client;

    @BeforeEach
    void setUp() {
        service = new TrainerService(userService, userRepository, connectionRepository,
                workOutRepository, workOutMapper, nutritionService);
        trainer = User.builder().id(1L).email("trainer@test.com").role(Role.ROLE_TRAINER).build();
        client = User.builder()
                .id(2L).email("client@test.com").role(Role.ROLE_USER)
                .userInfo(UserInfo.builder().firstName("Ivan").lastName("Petrov").build())
                .build();
    }

    /* ── getClients ───────────────────────────────────────── */

    @Test
    void getClients_returnsOwnersOfAcceptedTrainerConnections_queryExcludesFriendAndNonAccepted() {
        when(userService.getValidatedUserForAction("trainer@test.com")).thenReturn(trainer);
        when(connectionRepository.findAllByViewerAndTypeAndStatus(
                trainer, ConnectionType.TRAINER, ConnectionStatus.ACCEPTED))
                .thenReturn(List.of(trainerConnection(client)));
        when(workOutRepository.findAllByUserOrderByStartTimeDesc(client))
                .thenReturn(List.of(new WorkOut(), new WorkOut()));

        List<ClientDTO> clients = service.getClients("trainer@test.com");

        assertThat(clients).hasSize(1);
        ClientDTO dto = clients.get(0);
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getEmail()).isEqualTo("client@test.com");
        assertThat(dto.getFirstName()).isEqualTo("Ivan");
        assertThat(dto.getLastName()).isEqualTo("Petrov");
        assertThat(dto.getTotalWorkouts()).isEqualTo(2);
        // FRIEND / PENDING / REVOKED rows excluded at query level — exact-args verification:
        verify(connectionRepository).findAllByViewerAndTypeAndStatus(
                trainer, ConnectionType.TRAINER, ConnectionStatus.ACCEPTED);
    }

    @Test
    void getClients_roleUser_forbidden() {
        User regular = User.builder().id(5L).email("user@test.com").role(Role.ROLE_USER).build();
        when(userService.getValidatedUserForAction("user@test.com")).thenReturn(regular);

        assertBase(() -> service.getClients("user@test.com"),
                HttpStatus.FORBIDDEN, "Trainer role required");
    }

    @Test
    void getClients_gymOwner_allowed() {
        User gymOwner = User.builder().id(6L).email("gym@test.com").role(Role.ROLE_GYM_OWNER).build();
        when(userService.getValidatedUserForAction("gym@test.com")).thenReturn(gymOwner);
        when(connectionRepository.findAllByViewerAndTypeAndStatus(
                gymOwner, ConnectionType.TRAINER, ConnectionStatus.ACCEPTED))
                .thenReturn(List.of());

        assertThat(service.getClients("gym@test.com")).isEmpty();
    }

    /* ── getClientProgress ────────────────────────────────── */

    @Test
    void getClientProgress_withAcceptedTrainerConnection_returnsCompatibleClientProgressDTO() {
        DailyNutritionSummaryDTO summary = DailyNutritionSummaryDTO.builder().build();
        when(userService.getValidatedUserForAction("trainer@test.com")).thenReturn(trainer);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(client));
        when(connectionRepository.findByOwnerAndViewerAndType(client, trainer, ConnectionType.TRAINER))
                .thenReturn(Optional.of(trainerConnection(client)));
        // 60kg x 10 = 600
        when(workOutRepository.findAllByUserOrderByStartTimeDesc(client))
                .thenReturn(List.of(workout(set(60, 10))));
        when(workOutMapper.toDTO(any(WorkOut.class))).thenReturn(mock(WorkOutDTO.class));
        when(nutritionService.getDailySummary(any(LocalDate.class), eq("client@test.com"))).thenReturn(summary);

        ClientProgressDTO dto = service.getClientProgress(2L, "trainer@test.com");

        // same response shape as before the rewire
        assertThat(dto.getClient().getId()).isEqualTo(2L);
        assertThat(dto.getClient().getFirstName()).isEqualTo("Ivan");
        assertThat(dto.getWorkoutHistory()).hasSize(1);
        assertThat(dto.getTotalWorkouts()).isEqualTo(1);
        assertThat(dto.getTotalVolumeKg()).isEqualTo(600.0);
        assertThat(dto.getTodayNutrition()).isSameAs(summary);
    }

    @Test
    void getClientProgress_pendingTrainerConnection_forbidden() {
        UserConnection pending = trainerConnection(client);
        pending.setStatus(ConnectionStatus.PENDING);
        when(userService.getValidatedUserForAction("trainer@test.com")).thenReturn(trainer);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(client));
        when(connectionRepository.findByOwnerAndViewerAndType(client, trainer, ConnectionType.TRAINER))
                .thenReturn(Optional.of(pending));

        assertBase(() -> service.getClientProgress(2L, "trainer@test.com"),
                HttpStatus.FORBIDDEN, "This client is not assigned to you");
    }

    @Test
    void getClientProgress_revokedTrainerConnection_forbidden() {
        UserConnection revoked = trainerConnection(client);
        revoked.setStatus(ConnectionStatus.REVOKED);
        when(userService.getValidatedUserForAction("trainer@test.com")).thenReturn(trainer);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(client));
        when(connectionRepository.findByOwnerAndViewerAndType(client, trainer, ConnectionType.TRAINER))
                .thenReturn(Optional.of(revoked));

        assertBase(() -> service.getClientProgress(2L, "trainer@test.com"),
                HttpStatus.FORBIDDEN, "This client is not assigned to you");
    }

    @Test
    void getClientProgress_noConnection_forbidden() {
        when(userService.getValidatedUserForAction("trainer@test.com")).thenReturn(trainer);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(client));
        when(connectionRepository.findByOwnerAndViewerAndType(client, trainer, ConnectionType.TRAINER))
                .thenReturn(Optional.empty());

        assertBase(() -> service.getClientProgress(2L, "trainer@test.com"),
                HttpStatus.FORBIDDEN, "This client is not assigned to you");
    }

    @Test
    void getClientProgress_unknownClient_notFound() {
        when(userService.getValidatedUserForAction("trainer@test.com")).thenReturn(trainer);
        when(userRepository.findUserById(99L)).thenReturn(Optional.empty());

        assertBase(() -> service.getClientProgress(99L, "trainer@test.com"),
                HttpStatus.NOT_FOUND, "Client not found");
    }

    /* ── helpers ──────────────────────────────────────────── */

    private UserConnection trainerConnection(User client) {
        return UserConnection.builder()
                .id(20L).owner(client).viewer(trainer)
                .type(ConnectionType.TRAINER).status(ConnectionStatus.ACCEPTED)
                .canViewWorkouts(true).canViewProgressSummary(true)
                .canViewNutrition(true).canViewBodyMetrics(true)
                .build();
    }

    private static WorkOut workout(ExerciseSet... sets) {
        WorkOut workOut = new WorkOut();
        for (ExerciseSet s : sets) {
            workOut.addExerciseSet(s);
        }
        return workOut;
    }

    private static ExerciseSet set(double weight, int reps) {
        ExerciseSet s = new ExerciseSet();
        s.setWeight(weight);
        s.setReps(reps);
        return s;
    }

    private static void assertBase(org.assertj.core.api.ThrowableAssert.ThrowingCallable call,
                                   HttpStatus status, String message) {
        assertThatThrownBy(call)
                .isInstanceOf(BaseException.class)
                .satisfies(t -> {
                    BaseException e = (BaseException) t;
                    assertThat(e.getStatus()).isEqualTo(status);
                    assertThat(e.getMessage()).isEqualTo(message);
                });
    }
}
