package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Models.Enums.ActivityLevel;
import com.mycompany.fitnesstracker.Models.Enums.FitnessGoal;
import com.mycompany.fitnesstracker.Models.Enums.Sex;
import com.mycompany.fitnesstracker.Models.Nutrition.CalorieEstimateDTO;
import com.mycompany.fitnesstracker.Models.UserInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalorieCalculatorServiceTest {

    private final CalorieCalculatorService service = new CalorieCalculatorService();

    private UserInfo info(Integer heightCm, Double weightKg, LocalDate dob,
                          Sex sex, ActivityLevel activity, FitnessGoal goal) {
        return UserInfo.builder()
                .heightCm(heightCm)
                .weightKg(weightKg)
                .dateOfBirth(dob)
                .sex(sex)
                .activityLevel(activity)
                .fitnessGoal(goal)
                .build();
    }

    private LocalDate dobForAge(int age) {
        return LocalDate.now().minusYears(age).minusDays(1);
    }

    @Test
    void maleFormula_producesExpectedBmrTdeeAndSuggestions() {
        // 80 kg, 180 cm, age 30, MODERATE
        // BMR = 800 + 1125 - 150 + 5 = 1780 ; TDEE = 1780 * 1.55 = 2759
        CalorieEstimateDTO r = service.calculate(
                info(180, 80.0, dobForAge(30), Sex.MALE, ActivityLevel.MODERATE, FitnessGoal.MAINTAIN));

        assertTrue(r.isComplete());
        assertEquals(1780, r.getBmr());
        assertEquals(2760, r.getTdee());             // round10(2759)
        assertEquals(2760, r.getMaintainCalories());
        assertEquals(2350, r.getLoseCalories());     // round10(2759*0.85=2345.15)
        assertEquals(3030, r.getGainCalories());     // round10(2759*1.10=3034.9)
        assertEquals(2760, r.getSelectedGoalCalories()); // MAINTAIN
    }

    @Test
    void femaleFormula_usesMinus161Constant() {
        // 60 kg, 165 cm, age 25, MODERATE
        // BMR = 600 + 1031.25 - 125 - 161 = 1345.25 -> round10 = 1350
        CalorieEstimateDTO r = service.calculate(
                info(165, 60.0, dobForAge(25), Sex.FEMALE, ActivityLevel.MODERATE, FitnessGoal.MAINTAIN));

        assertTrue(r.isComplete());
        assertEquals(1350, r.getBmr());
        assertEquals(2090, r.getMaintainCalories()); // round10(1345.25*1.55=2085.14)
    }

    @Test
    void activityMultiplier_changesTdee() {
        CalorieEstimateDTO sedentary = service.calculate(
                info(165, 60.0, dobForAge(25), Sex.FEMALE, ActivityLevel.SEDENTARY, FitnessGoal.MAINTAIN));
        CalorieEstimateDTO active = service.calculate(
                info(165, 60.0, dobForAge(25), Sex.FEMALE, ActivityLevel.VERY_ACTIVE, FitnessGoal.MAINTAIN));

        assertTrue(sedentary.getMaintainCalories() < active.getMaintainCalories());
    }

    @Test
    void selectedGoal_followsFitnessGoal() {
        CalorieEstimateDTO lose = service.calculate(
                info(180, 80.0, dobForAge(30), Sex.MALE, ActivityLevel.MODERATE, FitnessGoal.LOSE));
        CalorieEstimateDTO gain = service.calculate(
                info(180, 80.0, dobForAge(30), Sex.MALE, ActivityLevel.MODERATE, FitnessGoal.GAIN));

        assertEquals(lose.getLoseCalories(), lose.getSelectedGoalCalories());
        assertEquals(gain.getGainCalories(), gain.getSelectedGoalCalories());
    }

    @Test
    void loseTarget_isFlooredForLowEnergyNeeds() {
        // 40 kg, 150 cm, age 60, SEDENTARY -> TDEE ~1052, lose ~890 -> floored to 1200 (female)
        CalorieEstimateDTO r = service.calculate(
                info(150, 40.0, dobForAge(60), Sex.FEMALE, ActivityLevel.SEDENTARY, FitnessGoal.LOSE));

        assertEquals(1200, r.getLoseCalories());
        assertEquals(1200, r.getSelectedGoalCalories());
    }

    @Test
    void missingRequiredField_returnsIncomplete() {
        CalorieEstimateDTO r = service.calculate(
                info(180, null, dobForAge(30), Sex.MALE, ActivityLevel.MODERATE, FitnessGoal.MAINTAIN));

        assertFalse(r.isComplete());
        assertNull(r.getMaintainCalories());
    }

    @Test
    void nullActivityAndGoal_defaultToModerateMaintain() {
        CalorieEstimateDTO r = service.calculate(
                info(180, 80.0, dobForAge(30), Sex.MALE, null, null));

        assertTrue(r.isComplete());
        assertEquals(2760, r.getMaintainCalories());      // MODERATE default
        assertEquals(r.getMaintainCalories(), r.getSelectedGoalCalories()); // MAINTAIN default
    }

    @Test
    void invalidAge_returnsIncomplete() {
        CalorieEstimateDTO r = service.calculate(
                info(180, 80.0, LocalDate.now(), Sex.MALE, ActivityLevel.MODERATE, FitnessGoal.MAINTAIN));

        assertFalse(r.isComplete());
    }

    @Test
    void outOfRangeHeightAndWeight_returnIncomplete() {
        CalorieEstimateDTO tallTooMuch = service.calculate(
                info(300, 80.0, dobForAge(30), Sex.MALE, ActivityLevel.MODERATE, FitnessGoal.MAINTAIN));
        CalorieEstimateDTO heavyTooMuch = service.calculate(
                info(180, 500.0, dobForAge(30), Sex.MALE, ActivityLevel.MODERATE, FitnessGoal.MAINTAIN));

        assertFalse(tallTooMuch.isComplete());
        assertFalse(heavyTooMuch.isComplete());
    }
}
