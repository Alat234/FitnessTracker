package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    public Optional<User> findUserByID(Long ID);

    public Optional<User> findUserByEmailIs(String email);


}
