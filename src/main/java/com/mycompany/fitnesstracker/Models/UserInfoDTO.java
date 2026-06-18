package com.mycompany.fitnesstracker.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mycompany.fitnesstracker.Models.Enums.ActivityLevel;
import com.mycompany.fitnesstracker.Models.Enums.FitnessGoal;
import com.mycompany.fitnesstracker.Models.Enums.Sex;

import java.time.LocalDate;

public record UserInfoDTO(
        String firstName,
        String lastName,
        String BIO,
        String phoneNumber,
        Integer heightCm,
        Double weightKg,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dateOfBirth,
        Sex sex,
        ActivityLevel activityLevel,
        FitnessGoal fitnessGoal
) {
}
