package com.healthcare.management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleInspectionRequest {

    @NotBlank(message = "Officer name is required")
    private String officer;

    @NotNull(message = "Scheduled inspection date and time is required")
    @Future(message = "Scheduled inspection date and time must be in the future")
    private LocalDateTime scheduledAt;

    @jakarta.validation.constraints.Size(max = 2000, message = "Notes must be at most 2000 characters")
    private String notes;
}
