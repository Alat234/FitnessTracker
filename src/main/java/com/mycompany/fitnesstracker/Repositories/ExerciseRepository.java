package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.WorkoutEntities.Exercise;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.ExerciseDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise,Long> {

}
