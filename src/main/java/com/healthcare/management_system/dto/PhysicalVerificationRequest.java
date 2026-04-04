package com.healthcare.management_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhysicalVerificationRequest {

    @NotBlank(message = "Officer name is required")
    private String officer;

    private boolean locationVerified = true;

    @NotBlank(message = "Verification notes are required")
    @jakarta.validation.constraints.Size(max = 2000, message = "Verification notes must be at most 2000 characters")
    private String notes;
}
