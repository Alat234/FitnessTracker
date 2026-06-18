package com.mycompany.fitnesstracker.Models;

import com.mycompany.fitnesstracker.Models.Enums.ActivityLevel;
import com.mycompany.fitnesstracker.Models.Enums.FitnessGoal;
import com.mycompany.fitnesstracker.Models.Enums.Sex;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name= "user_info")
public class UserInfo {

    @Id
    @Column(name = "user_info_id")
    private Long ID;
    @Column(name="user_first_name")
    private String firstName;
    @Column(name="user_last_name")
    private String lastName;
    @Column(name="user_bio")
    private  String bio;
    @Column(name="user_phone_number")
    private String phoneNumber;

    /* ── Body metrics (used by the calorie calculator) ── */
    @Column(name = "user_height_cm")
    private Integer heightCm;

    @Column(name = "user_weight_kg")
    private Double weightKg;

    @Column(name = "user_date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_sex")
    private Sex sex;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_activity_level")
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_fitness_goal")
    private FitnessGoal fitnessGoal;

    /* Gym the user selected from Discover (one per user). Separate from Gym.gymOwner. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_gym_id")
    private Gym gym;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name="user_id")
    private User userIdentity;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private  User trainer;

}
