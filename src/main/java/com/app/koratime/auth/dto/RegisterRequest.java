package com.app.koratime.auth.dto;

import com.app.koratime.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 20, message = "First name cannot exceed 20 characters")
        String firstName,
        @NotBlank(message = "Last name is required")
        @Size(max = 20, message = "Last name cannot exceed 20 characters")
        String lastName,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 50, message = "Password must be 8–50 characters")
        String password,
        @NotBlank(message = "Phone number is required")
        @Size(min = 11, max = 11, message = "Phone number must be 11 digits")
        String phoneNumber,
        @NotNull(message = "Role is required")
        Role role,

        String nationalId
) {
}
