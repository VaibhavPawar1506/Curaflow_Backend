package com.healthcare.management_system.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MedicalRecordResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long hospitalId;
    private String hospitalName;
    private String diagnosis;
    private String symptoms;
    private String prescription;
    private String notes;
    private Double weight;
    private Double height;
    private String bloodGroup;
    private String bloodPressure;
    private Integer heartbeat;
    private Integer bloodOxygen;
    private String prescriptionFile;
    private LocalDate recordDate;
    private LocalDate createdAt;
}
