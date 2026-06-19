package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.TrainingAppointment;
import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<TrainingAppointment, Long> {

    /** Appointments the trainer created, soonest first. */
    List<TrainingAppointment> findAllByTrainerOrderByStartAtAsc(User trainer);

    /** Appointments assigned to the client, soonest first. */
    List<TrainingAppointment> findAllByClientOrderByStartAtAsc(User client);
}
