package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Models.Appointment.AppointmentDTO;
import com.mycompany.fitnesstracker.Models.Appointment.CreateAppointmentRequest;
import com.mycompany.fitnesstracker.Models.Appointment.UpdateAppointmentRequest;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Connection.UserConnection;
import com.mycompany.fitnesstracker.Models.Enums.AppointmentStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionType;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.TrainingAppointment;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Repositories.AppointmentRepository;
import com.mycompany.fitnesstracker.Repositories.UserConnectionRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentServiceTest {

    private AppointmentRepository appointmentRepository;
    private UserConnectionRepository connectionRepository;
    private UserRepository userRepository;
    private UserService userService;
    private NotificationService notificationService;
    private AppointmentService service;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        connectionRepository = mock(UserConnectionRepository.class);
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        notificationService = mock(NotificationService.class);
        service = new AppointmentService(appointmentRepository, connectionRepository, userRepository, userService, notificationService);
    }

    private User user(long id, Role role) {
        return User.builder().id(id).email("u" + id + "@test.com").role(role).build();
    }

    private CreateAppointmentRequest createReq() {
        return CreateAppointmentRequest.builder()
                .title("Leg day")
                .startAt(LocalDateTime.of(2026, 7, 1, 10, 0))
                .endAt(LocalDateTime.of(2026, 7, 1, 11, 0))
                .notes("Bring belt")
                .build();
    }

    private void mockAcceptedConnection(User client, User trainer) {
        UserConnection conn = mock(UserConnection.class);
        when(conn.getStatus()).thenReturn(ConnectionStatus.ACCEPTED);
        when(connectionRepository.findByOwnerAndViewerAndType(client, trainer, ConnectionType.TRAINER))
                .thenReturn(Optional.of(conn));
    }

    /* ── Create ── */

    @Test
    void create_trainerForAcceptedClient_saves() {
        User trainer = user(1, Role.ROLE_TRAINER);
        User client = user(2, Role.ROLE_USER);
        when(userService.getUserByJWt()).thenReturn(trainer);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(client));
        mockAcceptedConnection(client, trainer);
        when(appointmentRepository.save(any(TrainingAppointment.class))).thenAnswer(inv -> {
            TrainingAppointment a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        AppointmentDTO dto = service.create(2L, createReq());

        assertEquals("Leg day", dto.title());
        assertEquals(AppointmentStatus.SCHEDULED, dto.status());
        assertEquals(2L, dto.client().id());
        verify(appointmentRepository).save(any(TrainingAppointment.class));
    }

    @Test
    void create_forNonClient_throwsForbidden_andDoesNotSave() {
        User trainer = user(1, Role.ROLE_TRAINER);
        User client = user(2, Role.ROLE_USER);
        when(userService.getUserByJWt()).thenReturn(trainer);
        when(userRepository.findUserById(2L)).thenReturn(Optional.of(client));
        when(connectionRepository.findByOwnerAndViewerAndType(client, trainer, ConnectionType.TRAINER))
                .thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> service.create(2L, createReq()));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_normalUser_throwsForbidden_andDoesNotLoadClient() {
        when(userService.getUserByJWt()).thenReturn(user(2, Role.ROLE_USER));

        assertThrows(BaseException.class, () -> service.create(3L, createReq()));
        verify(userRepository, never()).findUserById(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_endBeforeStart_throwsBadRequest() {
        when(userService.getUserByJWt()).thenReturn(user(1, Role.ROLE_TRAINER));
        CreateAppointmentRequest bad = CreateAppointmentRequest.builder()
                .title("X")
                .startAt(LocalDateTime.of(2026, 7, 1, 11, 0))
                .endAt(LocalDateTime.of(2026, 7, 1, 10, 0))
                .build();

        assertThrows(BaseException.class, () -> service.create(2L, bad));
        verify(userRepository, never()).findUserById(any());
        verify(appointmentRepository, never()).save(any());
    }

    /* ── List ── */

    @Test
    void listMy_trainer_returnsTrainerAppointments() {
        User trainer = user(1, Role.ROLE_TRAINER);
        when(userService.getUserByJWt()).thenReturn(trainer);
        TrainingAppointment a = new TrainingAppointment();
        a.setId(5L); a.setTitle("S"); a.setTrainer(trainer); a.setClient(user(2, Role.ROLE_USER));
        when(appointmentRepository.findAllByTrainerOrderByStartAtAsc(trainer)).thenReturn(List.of(a));

        List<AppointmentDTO> list = service.listMy();

        assertEquals(1, list.size());
        verify(appointmentRepository).findAllByTrainerOrderByStartAtAsc(trainer);
        verify(appointmentRepository, never()).findAllByClientOrderByStartAtAsc(any());
    }

    @Test
    void listMy_client_returnsClientAppointments() {
        User clientUser = user(2, Role.ROLE_USER);
        when(userService.getUserByJWt()).thenReturn(clientUser);
        TrainingAppointment a = new TrainingAppointment();
        a.setId(6L); a.setTitle("S"); a.setTrainer(user(1, Role.ROLE_TRAINER)); a.setClient(clientUser);
        when(appointmentRepository.findAllByClientOrderByStartAtAsc(clientUser)).thenReturn(List.of(a));

        List<AppointmentDTO> list = service.listMy();

        assertEquals(1, list.size());
        verify(appointmentRepository).findAllByClientOrderByStartAtAsc(clientUser);
        verify(appointmentRepository, never()).findAllByTrainerOrderByStartAtAsc(any());
    }

    /* ── Update / cancel ── */

    @Test
    void update_ownAppointment_appliesChanges() {
        User trainer = user(1, Role.ROLE_TRAINER);
        when(userService.getUserByJWt()).thenReturn(trainer);
        TrainingAppointment a = new TrainingAppointment();
        a.setId(7L); a.setTrainer(trainer); a.setClient(user(2, Role.ROLE_USER));
        a.setStartAt(LocalDateTime.of(2026, 7, 1, 10, 0)); a.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(7L)).thenReturn(Optional.of(a));
        when(appointmentRepository.save(a)).thenReturn(a);

        AppointmentDTO dto = service.update(7L, UpdateAppointmentRequest.builder()
                .title("Updated").status(AppointmentStatus.COMPLETED).build());

        assertEquals("Updated", dto.title());
        assertEquals(AppointmentStatus.COMPLETED, dto.status());
        verify(appointmentRepository).save(a);
    }

    @Test
    void update_anotherTrainersAppointment_throwsForbidden() {
        User trainer = user(1, Role.ROLE_TRAINER);
        when(userService.getUserByJWt()).thenReturn(trainer);
        TrainingAppointment a = new TrainingAppointment();
        a.setId(8L); a.setTrainer(user(99, Role.ROLE_TRAINER)); a.setClient(user(2, Role.ROLE_USER));
        when(appointmentRepository.findById(8L)).thenReturn(Optional.of(a));

        assertThrows(BaseException.class, () -> service.update(8L,
                UpdateAppointmentRequest.builder().title("X").build()));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void cancel_setsStatusCancelled_andPreservesRecord() {
        User trainer = user(1, Role.ROLE_TRAINER);
        when(userService.getUserByJWt()).thenReturn(trainer);
        TrainingAppointment a = new TrainingAppointment();
        a.setId(9L); a.setTrainer(trainer); a.setClient(user(2, Role.ROLE_USER));
        a.setStatus(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(9L)).thenReturn(Optional.of(a));

        service.cancel(9L);

        assertEquals(AppointmentStatus.CANCELLED, a.getStatus());
        verify(appointmentRepository).save(a);
        verify(appointmentRepository, never()).delete(any());
        verify(appointmentRepository, never()).deleteById(any());
    }

    @Test
    void cancel_anotherTrainersAppointment_throwsForbidden() {
        User trainer = user(1, Role.ROLE_TRAINER);
        when(userService.getUserByJWt()).thenReturn(trainer);
        TrainingAppointment a = new TrainingAppointment();
        a.setId(10L); a.setTrainer(user(99, Role.ROLE_TRAINER)); a.setClient(user(2, Role.ROLE_USER));
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));

        assertThrows(BaseException.class, () -> service.cancel(10L));
        verify(appointmentRepository, never()).save(any());
    }
}
