package com.mycompany.fitnesstracker.Models.GymEntities;

import com.mycompany.fitnesstracker.Models.Gym;
import com.mycompany.fitnesstracker.Models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Зв'язок зал ↔ тренер. Тренер закріплений за залом власником залу.
 * MVP: пряме призначення, без статусу прийняття.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "gym_trainers",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_gym_trainer",
                columnNames = {"gym_id", "trainer_id"}
        )
)
public class GymTrainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gym_trainer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
