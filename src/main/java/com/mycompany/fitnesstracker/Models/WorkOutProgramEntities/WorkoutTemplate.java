package com.mycompany.fitnesstracker.Models.WorkOutProgramEntities;

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
public class WorkoutTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_template_id")
    private Long id;

    private String name;
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "workout_program_id")
    private WorkoutProgram workoutProgram;
    @OneToMany(mappedBy = "workoutTemplate",cascade = CascadeType.ALL)
    private List<SetTemplate> setTemplates=new ArrayList<>();
    public void addSetTemplate(SetTemplate setTemplate){
        this.setTemplates.add(setTemplate);
        setTemplate.setWorkoutTemplate(this);
    }
    public void removeSetTemplate(SetTemplate set) {
        this.setTemplates.remove(set);
        set.setWorkoutTemplate(null);
    }
}
