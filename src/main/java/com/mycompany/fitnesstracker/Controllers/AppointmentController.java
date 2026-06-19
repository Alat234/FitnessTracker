package com.mycompany.fitnesstracker.Controllers;

import com.mycompany.fitnesstracker.Models.Appointment.AppointmentDTO;
import com.mycompany.fitnesstracker.Models.Appointment.CreateAppointmentRequest;
import com.mycompany.fitnesstracker.Models.Appointment.UpdateAppointmentRequest;
import com.mycompany.fitnesstracker.Services.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Scheduled workouts. Read for the current user (role-aware); write under
 * /api/trainer/* gated to the owning ROLE_TRAINER in AppointmentService.
 */
@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /** Current user's appointments — trainer-side if ROLE_TRAINER, else client-side. */
    @GetMapping("/api/appointments/my")
    public ResponseEntity<List<AppointmentDTO>> myAppointments() {
        return ResponseEntity.ok(appointmentService.listMy());
    }

    /** Trainer creates an appointment for a connected client. */
    @PostMapping("/api/trainer/clients/{clientId}/appointments")
    public ResponseEntity<AppointmentDTO> create(@PathVariable Long clientId,
                                                 @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(clientId, request));
    }

    /** Trainer updates title/time/notes/status of their own appointment. */
    @PutMapping("/api/trainer/appointments/{appointmentId}")
    public ResponseEntity<AppointmentDTO> update(@PathVariable Long appointmentId,
                                                 @RequestBody UpdateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.update(appointmentId, request));
    }

    /** Cancel = soft (status CANCELLED, record preserved). */
    @DeleteMapping("/api/trainer/appointments/{appointmentId}")
    public ResponseEntity<Void> cancel(@PathVariable Long appointmentId) {
        appointmentService.cancel(appointmentId);
        return ResponseEntity.noContent().build();
    }
}
