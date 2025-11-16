package com.mycompany.fitnesstracker.Models;

import com.mycompany.fitnesstracker.Models.Enums.RegistrationType;
import jakarta.validation.constraints.NotBlank;


public record AuthenticationRecord(
        @NotBlank(message="email can`t be empty")
        String email,
        @NotBlank(message="Password can`t be empty")
        String password

) {

}
