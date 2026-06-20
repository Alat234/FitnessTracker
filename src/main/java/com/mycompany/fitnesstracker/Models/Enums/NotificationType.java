package com.mycompany.fitnesstracker.Models.Enums;

/**
 * Type of an in-app notification. Personal types carry an {@code entityType}
 * pointing at the source (APPOINTMENT / CONNECTION / CHAT); SYSTEM_ANNOUNCEMENT
 * is created by an admin and fanned out (per-recipient row, {@code system=true}).
 */
public enum NotificationType {
    APPOINTMENT_ASSIGNED,
    APPOINTMENT_CANCELLED,
    APPOINTMENT_COMPLETED,
    CONNECTION_ACCEPTED,
    CONNECTION_DECLINED,
    CHAT_MESSAGE,
    SYSTEM_ANNOUNCEMENT
}
