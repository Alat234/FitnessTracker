package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.Gym;
import com.mycompany.fitnesstracker.Models.GymEntities.GymTrainer;
import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GymTrainerRepository extends JpaRepository<GymTrainer, Long> {

    List<GymTrainer> findAllByGym(Gym gym);

    Optional<GymTrainer> findByGymAndTrainer(Gym gym, User trainer);

    boolean existsByGymAndTrainer(Gym gym, User trainer);

    long countByGym(Gym gym);

    /** A trainer's gym (MVP: at most one) — for the Discover trainer's gym name. */
    Optional<GymTrainer> findFirstByTrainer(User trainer);
}
