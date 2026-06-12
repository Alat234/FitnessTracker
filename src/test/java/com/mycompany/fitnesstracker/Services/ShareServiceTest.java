package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.ConnectionMapper;
import com.mycompany.fitnesstracker.Mappers.WorkOutMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Connection.SharedProgressDTO;
import com.mycompany.fitnesstracker.Models.Connection.UserConnection;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionType;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.Nutrition.DailyNutritionSummaryDTO;
import com.mycompany.fitnesstracker.Models.User;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock
    private UserConnectionRepository connectionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private WorkOutRepository workOutRepository;
    @Mock
    private WorkOutMapper workOutMapper;
    @Mock
    private NutritionService nutritionService;

    private ShareService service;

    private User owner;
    private User viewer;

    @BeforeEach
    void setUp() {
        service = new ShareService(connectionRepository, userRepository, userService,
                workOutRepository, workOutMapper, nutritionService, new ConnectionMapper());
        owner  = User.builder().id(1L).email("owner@test.com").role(Role.ROLE_USER).build();
        viewer = User.builder().id(2L).email("viewer@test.com").role(Role.ROLE_USER).build();
    }

    /* ── happy paths ──────────────────────────────────────── */

    @Test
    void acceptedFriendConnection_returnsWorkoutsAndSummary_hidesNutrition() {
        stubViewerAndOwner();
        when(connectionRepository.findAllByOwnerIdAndViewerAndStatus(1L, viewer, ConnectionStatus.ACCEPTED))
                .thenReturn(List.of(friendConnection()));
        // 100kg x 5 + 80kg x 8 = 500 + 640 = 1140
        when(workOutRepository.findAllByUserOrderByStartTimeDesc(owner))
                .thenReturn(List.of(workout(set(100, 5)), workout(set(80, 8))));
        when(workOutMapper.toDTO(any(WorkOut.class))).thenReturn(mock(WorkOutDTO.class));

        SharedProgressDTO dto = service.getSharedProgress(1L, "viewer@test.com");

        assertThat(dto.getOwner().getId()).isEqualTo(1L);
        assertThat(dto.getOwner().getEmail()).isEqualTo("owner@test.com");
        assertThat(dto.getWorkoutHistory()).hasSize(2);
        assertThat(dto.getTotalWorkouts()).isEqualTo(2);
        assertThat(dto.getTotalVolumeKg()).isEqualTo(1140.0);
        assertThat(dto.getTodayNutrition()).isNull();
        assertThat(dto.getBodyMetrics()).isNull();
        assertThat(dto.getPermissions().isWorkouts()).isTrue();
        assertThat(dto.getPermissions().isProgressSummary()).isTrue();
        assertThat(dto.getPermissions().isNutrition()).isFalse();
        assertThat(dto.getPermissions().isBodyMetrics()).isFalse();
        verifyNoInteractions(nutritionService); // privacy: nutrition never even queried
    }

    @Test
    void acceptedTrainerConnection_includesNutrition() {
        DailyNutritionSummaryDTO summary = DailyNutritionSummaryDTO.builder().build();
        stubViewerAndOwner();
        when(connectionRepository.findAllByOwnerIdAndViewerAndStatus(1L, viewer, ConnectionStatus.ACCEPTED))
                .thenReturn(List.of(trainerConnection()));
        when(workOutRepository.findAllByUserOrderByStartTimeDesc(owner)).thenReturn(List.of());
        when(nutritionService.getDailySummary(any(LocalDate.class), eq("owner@test.com"))).thenReturn(summary);

        SharedProgressDTO dto = service.getSharedProgress(1L, "viewer@test.com");

        assertThat(dto.getTodayNutrition()).isSameAs(summary);
        assertThat(dto.getPermissions().isNutrition()).isTrue();
    }

    @Test
    void summaryOnlyPermission_hidesWorkoutHistory_keepsTotals() {
        UserConnection conn = friendConnection();
        conn.setCanViewWorkouts(false); // summary stays true
        stubViewerAndOwner();
        when(connectionRepository.findAllByOwnerIdAndViewerAndStatus(1L, viewer, ConnectionStatus.ACCEPTED))
                .thenReturn(List.of(conn));
        when(workOutRepository.findAllByUserOrderByStartTimeDesc(owner))
                .thenReturn(List.of(workout(set(50, 10))));

        SharedProgressDTO dto = service.getSharedProgress(1L, "viewer@test.com");

        assertThat(dto.getWorkoutHistory()).isNull();
        assertThat(dto.getTotalWorkouts()).isEqualTo(1);
        assertThat(dto.getTotalVolumeKg()).isEqualTo(500.0);
        verifyNoInteractions(workOutMapper);
    }

    @Test
    void friendAndTrainerRows_permissionsMergedWithOr() {
        DailyNutritionSummaryDTO summary = DailyNutritionSummaryDTO.builder().build();
        stubViewerAndOwner();
        when(connectionRepository.findAllByOwnerIdAndViewerAndStatus(1L, viewer, ConnectionStatus.ACCEPTED))
                .thenReturn(List.of(friendConnection(), trainerConnection()));
        when(workOutRepository.findAllByUserOrderByStartTimeDesc(owner)).thenReturn(List.of());
        when(nutritionService.getDailySummary(any(LocalDate.class), eq("owner@test.com"))).thenReturn(summary);

        SharedProgressDTO dto = service.getSharedProgress(1L, "viewer@test.com");

        assertThat(dto.getPermissions().isWorkouts()).isTrue();
        assertThat(dto.getPermissions().isNutrition()).isTrue();   // from TRAINER row
        assertThat(dto.getPermissions().isBodyMetrics()).isTrue(); // from TRAINER row
        assertThat(dto.getTodayNutrition()).isSameAs(summary);
    }

    @Test
    void bodyMetrics_staysNullEvenWhenPermitted_reservedPlaceholder() {
        stubViewerAndOwner();
        when(connectionRepository.findAllByOwnerIdAndViewerAndStatus(1L, viewer, ConnectionStatus.ACCEPTED))
                .thenReturn(List.of(trainerConnection()));
        when(workOutRepository.findAllByUserOrderByStartTimeDesc(owner)).thenReturn(List.of());
        when(nutritionService.getDailySummary(any(LocalDate.class), eq("owner@test.com")))
                .thenReturn(DailyNutritionSummaryDTO.builder().build());

        SharedProgressDTO dto = service.getSharedProgress(1L, "viewer@test.com");

        assertThat(dto.getPermissions().isBodyMetrics()).isTrue();
        assertThat(dto.getBodyMetrics()).isNull(); // not implemented yet — reserved
    }

    /* ── access denial ────────────────────────────────────── */

    @Test
    void noAcceptedConnection_forbidden() {
        // PENDING / DECLINED / REVOKED rows never reach the service:
        // the repository query is scoped to ACCEPTED, so they yield an empty list
        stubViewerAndOwner();
        when(connectionRepository.findAllByOwnerIdAndViewerAndStatus(1L, viewer, ConnectionStatus.ACCEPTED))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getSharedProgress(1L, "viewer@test.com"))
                .isInstanceOf(BaseException.class)
                .satisfies(t -> {
                    BaseException e = (BaseException) t;
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(e.getMessage()).isEqualTo("No access to this user's progress");
                });
        verify(connectionRepository).findAllByOwnerIdAndViewerAndStatus(1L, viewer, ConnectionStatus.ACCEPTED);
        verifyNoInteractions(workOutRepository, nutritionService);
    }

    @Test
    void unknownOwner_notFound() {
        when(userService.getValidatedUserForAction("viewer@test.com")).thenReturn(viewer);
        when(userRepository.findUserById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSharedProgress(99L, "viewer@test.com"))
                .isInstanceOf(BaseException.class)
                .satisfies(t -> {
                    BaseException e = (BaseException) t;
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(e.getMessage()).isEqualTo("User not found");
                });
    }

    /* ── helpers ──────────────────────────────────────────── */

    private void stubViewerAndOwner() {
        when(userService.getValidatedUserForAction("viewer@test.com")).thenReturn(viewer);
        when(userRepository.findUserById(1L)).thenReturn(Optional.of(owner));
    }

    private UserConnection friendConnection() {
        return UserConnection.builder()
                .id(10L).owner(owner).viewer(viewer)
                .type(ConnectionType.FRIEND).status(ConnectionStatus.ACCEPTED)
                .canViewWorkouts(true).canViewProgressSummary(true)
                .canViewNutrition(false).canViewBodyMetrics(false)
                .build();
    }

    private UserConnection trainerConnection() {
        return UserConnection.builder()
                .id(11L).owner(owner).viewer(viewer)
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
}
