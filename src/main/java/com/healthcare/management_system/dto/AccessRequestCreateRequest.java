package com.healthcare.management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccessRequestCreateRequest {

    @NotBlank(message = "Requested hospital name is required")
    @Size(max = 150, message = "Requested hospital name must be at most 150 characters")
    private String requestedHospitalName;

    @Size(max = 50, message = "Requested hospital code must be at most 50 characters")
    private String requestedHospitalCode;

    @NotBlank(message = "Purpose is required")
    @Size(max = 1000, message = "Purpose must be at most 1000 characters")
    private String purpose;

    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String notes;
}
