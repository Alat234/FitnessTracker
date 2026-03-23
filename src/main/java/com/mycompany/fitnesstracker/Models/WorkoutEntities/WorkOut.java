package com.mycompany.fitnesstracker.Models.WorkoutEntities;

import com.mycompany.fitnesstracker.Models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WorkOut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_id")
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "workOut",cascade = CascadeType.ALL,fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ExerciseSet> exerciseSets =new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    public void addExerciseSet(ExerciseSet exerciseSet){
        this.exerciseSets.add(exerciseSet);
        exerciseSet.setWorkOut(this);
    }
    public void removeExerciseSet(ExerciseSet exerciseSet){
        this.exerciseSets.remove(exerciseSet);
        exerciseSet.setWorkOut(null);
    }
}
