package com.mycompany.fitnesstracker.Models.Nutrition;

import com.mycompany.fitnesstracker.Models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Одиничний запис прийому їжі (одна страва за один день).
 * Користувач може мати багато записів за один день — кожна страва окремо.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "nutrition_logs",
        indexes = {
                @Index(name = "idx_nutrition_user_date", columnList = "user_id,date")
        }
)
public class NutritionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nutrition_log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "food_name", nullable = false, length = 200)
    private String foodName;

    /* Скільки одиниць було спожито (вага / об'єм / штуки) */
    @Column(name = "quantity")
    private Double quantity;

    /* g, ml, oz, pcs, serving — рядок, бо може розширятися */
    @Column(name = "unit", length = 16)
    private String unit;

    @Column(name = "calories", nullable = false)
    private Double calories;

    @Column(name = "protein")
    private Double protein;

    @Column(name = "carbohydrates")
    private Double carbohydrates;

    @Column(name = "fat")
    private Double fat;

    /* Дата прийому їжі — окрема колонка для швидкої фільтрації по дню */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /* Час створення запису */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (date == null) date = LocalDate.now();
    }
}
