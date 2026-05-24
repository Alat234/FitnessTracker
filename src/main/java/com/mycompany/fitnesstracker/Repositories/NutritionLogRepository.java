package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.Nutrition.NutritionLog;
import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface NutritionLogRepository extends JpaRepository<NutritionLog, Long> {

    /* Усі записи користувача за конкретний день, відсортовані за часом створення */
    List<NutritionLog> findAllByUserAndDateOrderByCreatedAtAsc(User user, LocalDate date);

    /* Усі записи між двома датами (для звітів тренеру тощо) */
    List<NutritionLog> findAllByUserAndDateBetweenOrderByDateDesc(User user, LocalDate from, LocalDate to);
}
