package com.mycompany.fitnesstracker.Models.Connection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Права перегляду даних власника у межах зв'язку.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionsDTO {
    private boolean workouts;
    private boolean nutrition;
    private boolean bodyMetrics;
    private boolean progressSummary;
}
