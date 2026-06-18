package com.mycompany.fitnesstracker.Models;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Personal profile data the user may edit. Intentionally excludes email, role,
 * id and password — those are never updatable through this request.
 */
@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @Size(max = 30, message = "Phone number must be at most 30 characters")
    private String phoneNumber;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;
}
