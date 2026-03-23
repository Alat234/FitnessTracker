package com.mycompany.fitnesstracker.Models.WorkOutProgramEntities;

import com.mycompany.fitnesstracker.Models.WorkoutEntities.Exercise;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOut;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SetTemplate {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private int reps;
    private double weight;
    @ManyToOne
    @JoinColumn(name = "workout_template_id",nullable = false)
    private WorkoutTemplate workoutTemplate;
    @ManyToOne
    @JoinColumn(name = "exercise_id",nullable = false)
    private Exercise exercise;


}
