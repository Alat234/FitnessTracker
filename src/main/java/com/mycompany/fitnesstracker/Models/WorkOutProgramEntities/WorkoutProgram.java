package com.mycompany.fitnesstracker.Models.WorkOutProgramEntities;

import com.mycompany.fitnesstracker.Models.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class WorkoutProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_program_id")
    private Long id;

    private String name;

    private int dayAWeek;

    private String goal;
    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "user_id", nullable = false)
    private User user;
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
