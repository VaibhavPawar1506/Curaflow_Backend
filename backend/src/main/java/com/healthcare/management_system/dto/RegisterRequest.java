package com.healthcare.management_system.dto;

import com.healthcare.management_system.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(min = 7, max = 20, message = "Phone number must be between 7 and 20 characters")
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Hospital code is required")
    private String hospitalCode;

    // Doctor-specific fields (optional, required only when role = DOCTOR)
    @Size(max = 120, message = "Specialization must be at most 120 characters")
    private String specialization;

    @Size(max = 50, message = "License number must be at most 50 characters")
    private String licenseNumber;
}
