package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.WorkOutProgramEntities.WorkoutProgram;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface WorkOutRepository extends JpaRepository<WorkOut, Long> {
    List<WorkOut> findAllByUserOrderByStartTimeDesc(User user);
}
