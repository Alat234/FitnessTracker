package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.Gym;
import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GymRepository extends JpaRepository<Gym, Long> {

    /** Зал поточного власника (MVP: один зал на власника). */
    Optional<Gym> findByGymOwner(User gymOwner);

    boolean existsByGymOwner(User gymOwner);
}
