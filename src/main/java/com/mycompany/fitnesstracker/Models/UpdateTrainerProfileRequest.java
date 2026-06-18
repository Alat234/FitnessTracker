package com.mycompany.fitnesstracker.Models;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Public trainer profile fields a ROLE_TRAINER user may edit. Both optional.
 * Never carries email, phone, role, password, or private/client data.
 */
@Getter
@Setter
public class UpdateTrainerProfileRequest {

    @Size(max = 100, message = "Specialization must be at most 100 characters")
    private String specialization;

    @Size(max = 512, message = "Image URL must be at most 512 characters")
    private String imageUrl;
}
