package com.healthcare.management_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InsuranceDetailsRequest {
    @NotBlank(message = "Provider name is required")
    private String providerName;

    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    private String groupNumber;
}
