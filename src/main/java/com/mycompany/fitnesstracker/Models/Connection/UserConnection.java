package com.mycompany.fitnesstracker.Models.Connection;

import com.mycompany.fitnesstracker.Models.Enums.ConnectionStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionType;
import com.mycompany.fitnesstracker.Models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Зв'язок між користувачами для шерингу прогресу.
 * owner — той, чиї дані показуються; viewer — той, хто отримує доступ (друг або тренер).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "user_connection",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_owner_viewer_type",
                columnNames = {"owner_id", "viewer_id", "connection_type"}
        )
)
public class UserConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_connection_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viewer_id", nullable = false)
    private User viewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false)
    private ConnectionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConnectionStatus status;

    @Column(name = "can_view_workouts", nullable = false)
    private boolean canViewWorkouts;

    @Column(name = "can_view_nutrition", nullable = false)
    private boolean canViewNutrition;

    @Column(name = "can_view_body_metrics", nullable = false)
    private boolean canViewBodyMetrics;

    @Column(name = "can_view_progress_summary", nullable = false)
    private boolean canViewProgressSummary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /* Час відповіді: accept / decline / revoke */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ConnectionStatus.PENDING;
    }
}
