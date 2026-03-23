package com.mycompany.fitnesstracker.Models.WorkOutProgramEntities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WorkoutProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_program_id")
    private Long id;

    private String name;

    private int dayAWeek;

    private String goal;
    @OneToMany(mappedBy = "workoutProgram",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<WorkoutTemplate> workoutTemplates= new ArrayList<>();
    public void addWorkOutTemplate(WorkoutTemplate workoutTemplate){
        this.workoutTemplates.add(workoutTemplate);
        workoutTemplate.setWorkoutProgram(this);

    }
    public void  removeWorkOutTemplate(WorkoutTemplate workoutTemplate){
        this.workoutTemplates.remove(workoutTemplate);
        workoutTemplate.setWorkoutProgram(null);
    }
}
