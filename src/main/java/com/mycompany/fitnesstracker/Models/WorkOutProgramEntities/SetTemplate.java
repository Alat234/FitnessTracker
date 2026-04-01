package com.mycompany.fitnesstracker.Models.WorkOutProgramEntities;

import com.mycompany.fitnesstracker.Models.WorkoutEntities.Exercise;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.WorkOut;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
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
