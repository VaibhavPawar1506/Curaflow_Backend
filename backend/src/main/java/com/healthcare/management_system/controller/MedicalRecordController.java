package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.MedicalRecordRequest;
import com.healthcare.management_system.dto.MedicalRecordResponse;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.service.AuditLogService;
import com.healthcare.management_system.service.MedicalRecordService;
import com.healthcare.management_system.service.PatientService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Medical Records", description = "Clinical records and prescription-linked patient history APIs")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final AuditLogService auditLogService;
    private final PatientService patientService;

    @PostMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> addRecord(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalRecordRequest request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicalRecordService.addRecord(patientId, request));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getPatientRecords(
            @AuthenticationPrincipal User user,
            @PathVariable Long patientId) {
        ApiResponse<List<MedicalRecordResponse>> response = medicalRecordService.getPatientRecords(patientId);
        if ("PATIENT".equals(user.getRole().name())) {
            auditLogService.log(user, patientService.getPatientEntity(user), "VIEW_MEDICAL_RECORDS", "MEDICAL_RECORD_LIST",
                    String.valueOf(patientId), "Patient viewed their medical records");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getRecordById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        ApiResponse<MedicalRecordResponse> response = medicalRecordService.getRecordById(id);
        if ("PATIENT".equals(user.getRole().name())) {
            auditLogService.log(user, patientService.getPatientEntity(user), "VIEW_MEDICAL_RECORD", "MEDICAL_RECORD",
                    String.valueOf(id), "Patient viewed a medical record");
        }
        return ResponseEntity.ok(response);
    }
}
