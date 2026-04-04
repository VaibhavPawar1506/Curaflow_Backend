package com.healthcare.management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MedicalRecordRequest {

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 2000, message = "Diagnosis must be at most 2000 characters")
    private String diagnosis;
    
    private Long doctorId; // Optional, used by staff to assign a doctor
    
    @Size(max = 2000, message = "Symptoms must be at most 2000 characters")
    private String symptoms;

    @Size(max = 2000, message = "Prescription must be at most 2000 characters")
    private String prescription;

    @Size(max = 255, message = "Prescription file name must be at most 255 characters")
    private String prescriptionFile;

    @Size(max = 4000, message = "Notes must be at most 4000 characters")
    private String notes;

    @Positive(message = "Weight must be greater than zero")
    private Double weight;

    @Positive(message = "Height must be greater than zero")
    private Double height;

    @Size(max = 10, message = "Blood group must be at most 10 characters")
    private String bloodGroup;

    @Size(max = 20, message = "Blood pressure must be at most 20 characters")
    private String bloodPressure;

    @PositiveOrZero(message = "Heartbeat cannot be negative")
    private Integer heartbeat;

    @PositiveOrZero(message = "Blood oxygen cannot be negative")
    private Integer bloodOxygen;
}
