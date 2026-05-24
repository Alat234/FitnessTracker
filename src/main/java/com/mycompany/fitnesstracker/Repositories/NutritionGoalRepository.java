package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.Nutrition.NutritionGoal;
import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionGoalRepository extends JpaRepository<NutritionGoal, Long> {
    Optional<NutritionGoal> findByUser(User user);
}
