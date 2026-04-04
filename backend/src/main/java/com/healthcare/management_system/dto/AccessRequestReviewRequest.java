package com.healthcare.management_system.dto;

import com.healthcare.management_system.enums.AccessRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccessRequestReviewRequest {

    @NotNull(message = "Status is required")
    private AccessRequestStatus status;

    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String notes;
}
