package com.mycompany.fitnesstracker.Models.WorkoutEntities;

import com.mycompany.fitnesstracker.Models.Enums.BodyPart;
import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseDTO {
    private Long id;
    private String name;
    private BodyPart bodyPart;
    private String description;
    private String imageUrl;
}
