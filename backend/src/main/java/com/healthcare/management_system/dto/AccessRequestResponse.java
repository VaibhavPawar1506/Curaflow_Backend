package com.healthcare.management_system.dto;

import com.healthcare.management_system.enums.AccessRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccessRequestResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private String requestedHospitalName;
    private String requestedHospitalCode;
    private String purpose;
    private AccessRequestStatus status;
    private String notes;
    private String reviewedByName;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
