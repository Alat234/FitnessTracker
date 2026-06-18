package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.Gym;
import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GymRepository extends JpaRepository<Gym, Long> {

    /** Зал поточного власника (MVP: один зал на власника). */
    Optional<Gym> findByGymOwner(User gymOwner);

    boolean existsByGymOwner(User gymOwner);

    /** Public Discover directory. Null isPublic (legacy rows) is treated as public. */
    @Query("select g from Gym g where g.isPublic is null or g.isPublic = true order by g.name asc")
    List<Gym> findPublicGyms();
}
