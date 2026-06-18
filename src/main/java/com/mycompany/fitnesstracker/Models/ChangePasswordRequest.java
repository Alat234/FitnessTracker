package com.mycompany.fitnesstracker.Models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Password change payload. The password is verified and re-encoded server-side
 * and is never echoed back in any response.
 */
@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    private String newPassword;
}
