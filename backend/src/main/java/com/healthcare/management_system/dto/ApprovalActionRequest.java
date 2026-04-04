package com.healthcare.management_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalActionRequest {

    @NotBlank(message = "Notes are required")
    @jakarta.validation.constraints.Size(max = 2000, message = "Notes must be at most 2000 characters")
    private String notes;
}
