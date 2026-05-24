package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserById(Long id);
    Optional<User> findUserByEmailIs(String email);

    /**
     * Усі юзери, у яких в UserInfo призначено цього тренера.
     * Робимо через явний JPQL, бо UserInfo має @MapsId і derived-query
     * по userInfo.trainer не завжди працює стабільно при lazy-loaded one-to-one.
     */
    @Query("select u from User u join u.userInfo info where info.trainer = :trainer")
    List<User> findAllClientsByTrainer(@Param("trainer") User trainer);
}
