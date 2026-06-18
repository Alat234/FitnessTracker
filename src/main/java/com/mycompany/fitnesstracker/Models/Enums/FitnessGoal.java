package com.mycompany.fitnesstracker.Models.Enums;

/**
 * Calorie goal applied to TDEE: lose (-15%), maintain (0%), gain (+10%).
 */
public enum FitnessGoal {
    LOSE(0.85),
    MAINTAIN(1.0),
    GAIN(1.10);

    private final double factor;

    FitnessGoal(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}
