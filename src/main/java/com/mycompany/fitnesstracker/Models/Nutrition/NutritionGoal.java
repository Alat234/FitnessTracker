package com.mycompany.fitnesstracker.Models.Nutrition;

import com.mycompany.fitnesstracker.Models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Денні цілі по макронутрієнтах для конкретного користувача.
 * Зв'язок 1:1 із User через @MapsId — primary key береться з user_id.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "nutrition_goals")
public class NutritionGoal {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "calorie_goal", nullable = false)
    private Integer calorieGoal;

    @Column(name = "protein_goal", nullable = false)
    private Integer proteinGoal;

    @Column(name = "carbs_goal", nullable = false)
    private Integer carbsGoal;

    @Column(name = "fat_goal", nullable = false)
    private Integer fatGoal;

    /* Дефолтні значення для нового профілю */
    public static NutritionGoal defaultsFor(User user) {
        return NutritionGoal.builder()
                .user(user)
                .calorieGoal(2000)
                .proteinGoal(150)
                .carbsGoal(250)
                .fatGoal(70)
                .build();
    }
}
