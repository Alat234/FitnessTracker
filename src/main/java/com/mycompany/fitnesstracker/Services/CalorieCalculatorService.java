package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Models.Enums.ActivityLevel;
import com.mycompany.fitnesstracker.Models.Enums.FitnessGoal;
import com.mycompany.fitnesstracker.Models.Enums.Sex;
import com.mycompany.fitnesstracker.Models.Nutrition.CalorieEstimateDTO;
import com.mycompany.fitnesstracker.Models.UserInfo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * Daily calorie estimate using the Mifflin-St Jeor BMR formula.
 *   male:   BMR = 10*kg + 6.25*cm - 5*age + 5
 *   female: BMR = 10*kg + 6.25*cm - 5*age - 161
 *   TDEE = BMR * activity multiplier
 * Suggestions: maintain = TDEE, lose = -15%, gain = +10%. All values rounded to
 * the nearest 10 kcal. The "lose" target is floored (female >= 1200, male >= 1500).
 * This is an estimate, not medical advice.
 */
@Service
public class CalorieCalculatorService {

    public CalorieEstimateDTO calculate(UserInfo info) {
        if (info == null
                || info.getHeightCm() == null
                || info.getWeightKg() == null
                || info.getDateOfBirth() == null
                || info.getSex() == null) {
            return incomplete("Add your body metrics (height, weight, date of birth, sex) to see your calorie estimate.");
        }

        int age = Period.between(info.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 13 || age > 100) {
            return incomplete("Date of birth looks invalid. Age must be between 13 and 100.");
        }

        int heightCm = info.getHeightCm();
        if (heightCm < 100 || heightCm > 250) {
            return incomplete("Height must be between 100 and 250 cm.");
        }

        double weightKg = info.getWeightKg();
        if (weightKg < 30 || weightKg > 300) {
            return incomplete("Weight must be between 30 and 300 kg.");
        }

        double bmr = 10 * weightKg + 6.25 * heightCm - 5 * age
                + (info.getSex() == Sex.MALE ? 5 : -161);

        ActivityLevel activity = info.getActivityLevel() != null
                ? info.getActivityLevel()
                : ActivityLevel.MODERATE;
        double tdee = bmr * activity.getMultiplier();

        int maintain = round10(tdee);
        int loseFloor = info.getSex() == Sex.MALE ? 1500 : 1200;
        int lose = Math.max(round10(tdee * FitnessGoal.LOSE.getFactor()), loseFloor);
        int gain = round10(tdee * FitnessGoal.GAIN.getFactor());

        FitnessGoal goal = info.getFitnessGoal() != null
                ? info.getFitnessGoal()
                : FitnessGoal.MAINTAIN;
        int selected = switch (goal) {
            case LOSE -> lose;
            case GAIN -> gain;
            case MAINTAIN -> maintain;
        };

        return CalorieEstimateDTO.builder()
                .bmr(round10(bmr))
                .tdee(round10(tdee))
                .maintainCalories(maintain)
                .loseCalories(lose)
                .gainCalories(gain)
                .selectedGoalCalories(selected)
                .complete(true)
                .message("Estimate based on the Mifflin-St Jeor formula. This is not medical advice.")
                .build();
    }

    private CalorieEstimateDTO incomplete(String message) {
        return CalorieEstimateDTO.builder()
                .complete(false)
                .message(message)
                .build();
    }

    private int round10(double value) {
        return (int) (Math.round(value / 10.0) * 10);
    }
}
