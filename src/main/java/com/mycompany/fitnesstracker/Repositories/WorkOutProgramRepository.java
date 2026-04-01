package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.WorkOutProgramEntities.WorkoutProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkOutProgramRepository extends JpaRepository<WorkoutProgram, Long> {
    List<WorkoutProgram> findAllByUser(User user);
}
