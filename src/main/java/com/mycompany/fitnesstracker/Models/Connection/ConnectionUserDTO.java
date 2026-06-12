package com.mycompany.fitnesstracker.Models.Connection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Коротка інформація про користувача у зв'язку (без чутливих полів).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionUserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
}
