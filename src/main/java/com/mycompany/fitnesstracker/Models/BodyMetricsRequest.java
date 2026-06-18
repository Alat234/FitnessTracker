package com.mycompany.fitnesstracker.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mycompany.fitnesstracker.Models.Enums.ActivityLevel;
import com.mycompany.fitnesstracker.Models.Enums.FitnessGoal;
import com.mycompany.fitnesstracker.Models.Enums.Sex;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Body metrics used by the calorie calculator. All fields are optional so a user
 * can save a partial profile; validation annotations only fire on non-null values.
 */
@Getter
@Setter
public class BodyMetricsRequest {

    @Min(value = 100, message = "Height must be at least 100 cm")
    @Max(value = 250, message = "Height must be at most 250 cm")
    private Integer heightCm;

    @DecimalMin(value = "30.0", message = "Weight must be at least 30 kg")
    @DecimalMax(value = "300.0", message = "Weight must be at most 300 kg")
    private Double weightKg;

    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Sex sex;

    private ActivityLevel activityLevel;

    private FitnessGoal fitnessGoal;
}
