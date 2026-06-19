package com.mycompany.fitnesstracker.Models;

import com.mycompany.fitnesstracker.Models.Enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A workout session a trainer schedules for a connected client. Scheduling only —
 * never creates a workout log. Trainer manages; client views (read-only).
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "training_appointments")
public class TrainingAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private User trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;

    @Column(name = "appointment_title")
    private String title;

    @Column(name = "appointment_start_at")
    private LocalDateTime startAt;

    @Column(name = "appointment_end_at")
    private LocalDateTime endAt;

    @Column(name = "appointment_notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_status")
    private AppointmentStatus status;

    @Column(name = "appointment_created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "appointment_updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (status == null) status = AppointmentStatus.SCHEDULED;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
