package com.healthcare.management_system.dto;

import com.healthcare.management_system.enums.LabAppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabAppointmentResponse {
    private Long id;
    private Long labId;
    private String labName;
    private LocalDateTime appointmentDateTime;
    private LabAppointmentStatus status;
    private Boolean homeCollection;
    private String notes;
    private List<String> selectedTests;
    private LocalDateTime createdAt;
}
