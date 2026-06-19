package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Models.Appointment.AppointmentDTO;
import com.mycompany.fitnesstracker.Models.Appointment.AppointmentPartyDTO;
import com.mycompany.fitnesstracker.Models.Appointment.CreateAppointmentRequest;
import com.mycompany.fitnesstracker.Models.Appointment.UpdateAppointmentRequest;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Enums.AppointmentStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionType;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.TrainingAppointment;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.UserInfo;
import com.mycompany.fitnesstracker.Repositories.AppointmentRepository;
import com.mycompany.fitnesstracker.Repositories.UserConnectionRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trainer-client scheduled workouts. Scheduling only — never creates workout logs.
 * Role gating is service-layer (project does not enable method security). Only
 * ROLE_TRAINER may create/update/cancel; clients read their own appointments.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository      appointmentRepository;
    private final UserConnectionRepository   connectionRepository;
    private final UserRepository             userRepository;
    private final UserService                userService;

    /* ── Create (trainer → connected client) ───────────────── */
    @Transactional
    public AppointmentDTO create(Long clientId, CreateAppointmentRequest request) {
        User trainer = requireTrainer();
        validateTitle(request.getTitle());
        if (request.getStartAt() == null) {
            throw new BaseException("Start time is required", HttpStatus.BAD_REQUEST);
        }
        validateRange(request.getStartAt(), request.getEndAt());

        User client = userRepository.findUserById(clientId)
                .orElseThrow(() -> new BaseException("Client not found", HttpStatus.NOT_FOUND));
        requireAcceptedConnection(client, trainer);

        TrainingAppointment appt = new TrainingAppointment();
        appt.setTrainer(trainer);
        appt.setClient(client);
        appt.setTitle(request.getTitle().trim());
        appt.setStartAt(request.getStartAt());
        appt.setEndAt(request.getEndAt());
        appt.setNotes(request.getNotes());
        appt.setStatus(AppointmentStatus.SCHEDULED);

        return toDTO(appointmentRepository.save(appt));
    }

    /* ── List current user's appointments (role-aware) ─────── */
    @Transactional
    public List<AppointmentDTO> listMy() {
        User user = userService.getUserByJWt();
        List<TrainingAppointment> appts = user.getRole() == Role.ROLE_TRAINER
                ? appointmentRepository.findAllByTrainerOrderByStartAtAsc(user)
                : appointmentRepository.findAllByClientOrderByStartAtAsc(user);
        return appts.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /* ── Update (owning trainer only) ──────────────────────── */
    @Transactional
    public AppointmentDTO update(Long appointmentId, UpdateAppointmentRequest request) {
        User trainer = requireTrainer();
        TrainingAppointment appt = loadOwnedByTrainer(appointmentId, trainer);

        if (request.getTitle() != null) {
            validateTitle(request.getTitle());
            appt.setTitle(request.getTitle().trim());
        }
        if (request.getStartAt() != null) appt.setStartAt(request.getStartAt());
        if (request.getEndAt() != null) appt.setEndAt(request.getEndAt());
        // re-validate against the effective (possibly partially updated) values
        validateRange(appt.getStartAt(), appt.getEndAt());
        if (request.getNotes() != null) appt.setNotes(request.getNotes());
        if (request.getStatus() != null) appt.setStatus(request.getStatus());

        return toDTO(appointmentRepository.save(appt));
    }

    /* ── Cancel = soft (status CANCELLED, record preserved) ── */
    @Transactional
    public void cancel(Long appointmentId) {
        User trainer = requireTrainer();
        TrainingAppointment appt = loadOwnedByTrainer(appointmentId, trainer);
        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appt);
    }

    /* ── guards / validation ───────────────────────────────── */

    /** Only ROLE_TRAINER may manage appointments (admin/gym-owner do NOT bypass). */
    private User requireTrainer() {
        User user = userService.getUserByJWt();
        if (user.getRole() != Role.ROLE_TRAINER) {
            throw new BaseException("Trainer role required", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    /** Accepted TRAINER connection: owner = client, viewer = trainer. No legacy UserInfo.trainer. */
    private void requireAcceptedConnection(User client, User trainer) {
        boolean ok = connectionRepository
                .findByOwnerAndViewerAndType(client, trainer, ConnectionType.TRAINER)
                .filter(c -> c.getStatus() == ConnectionStatus.ACCEPTED)
                .isPresent();
        if (!ok) {
            throw new BaseException("This client is not assigned to you", HttpStatus.FORBIDDEN);
        }
    }

    private TrainingAppointment loadOwnedByTrainer(Long appointmentId, User trainer) {
        TrainingAppointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BaseException("Appointment not found", HttpStatus.NOT_FOUND));
        if (appt.getTrainer() == null || !appt.getTrainer().getId().equals(trainer.getId())) {
            throw new BaseException("This appointment does not belong to you", HttpStatus.FORBIDDEN);
        }
        return appt;
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BaseException("Title is required", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) {
            throw new BaseException("End time must be after start time", HttpStatus.BAD_REQUEST);
        }
    }

    /* ── mapping ───────────────────────────────────────────── */
    private AppointmentDTO toDTO(TrainingAppointment a) {
        return AppointmentDTO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .startAt(a.getStartAt())
                .endAt(a.getEndAt())
                .notes(a.getNotes())
                .status(a.getStatus())
                .trainer(toParty(a.getTrainer()))
                .client(toParty(a.getClient()))
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private AppointmentPartyDTO toParty(User user) {
        if (user == null) return null;
        UserInfo info = user.getUserInfo();
        return AppointmentPartyDTO.builder()
                .id(user.getId())
                .firstName(info != null ? info.getFirstName() : null)
                .lastName(info != null ? info.getLastName() : null)
                .email(user.getEmail())
                .build();
    }
}
