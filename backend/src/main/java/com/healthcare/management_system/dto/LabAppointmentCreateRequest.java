package com.healthcare.management_system.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LabAppointmentCreateRequest {
    @NotNull
    private Long labId;

    @NotEmpty
    private List<String> selectedTests;

    @NotNull
    @Future
    private LocalDateTime appointmentDateTime;

    @NotNull
    private Boolean homeCollection;

    private String notes;
}
