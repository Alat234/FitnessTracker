package com.mycompany.fitnesstracker.Models.WorkoutEntities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ExerciseSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int reps;
    private double weight;
    @ManyToOne
    @JoinColumn(name = "workout_id")
    private WorkOut workOut;
    @ManyToOne
    @JoinColumn(name = "exercise_id",nullable = false)
    private Exercise exercise;
}
