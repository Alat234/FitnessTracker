package com.mycompany.fitnesstracker.Models.Connection;

import com.mycompany.fitnesstracker.Models.Enums.ConnectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Запит на створення запрошення: email отримувача + тип зв'язку.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendInviteRequest {
    private String email;
    private ConnectionType type;
}
