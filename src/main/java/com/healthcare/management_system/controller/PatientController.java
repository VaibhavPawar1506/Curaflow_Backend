package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.AccessRequestCreateRequest;
import com.healthcare.management_system.dto.AccessRequestResponse;
import com.healthcare.management_system.dto.AuditLogResponse;
import com.healthcare.management_system.dto.PatientResponse;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.service.AccessRequestService;
import com.healthcare.management_system.service.AuditLogService;
import com.healthcare.management_system.service.PatientService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.healthcare.management_system.dto.UserProfileUpdateRequest;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patients", description = "Patient profile and hospital-scoped patient management APIs")
public class PatientController {

    private final PatientService patientService;
    private final AccessRequestService accessRequestService;
    private final AuditLogService auditLogService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> getMyProfile(
            @AuthenticationPrincipal User user) {
        PatientResponse response = patientService.getPatientProfile(user);
        auditLogService.log(user, patientService.getPatientEntity(user), "VIEW_PROFILE", "PATIENT_PROFILE",
                String.valueOf(response.getId()), "Patient viewed their profile");
        return ResponseEntity.ok(ApiResponse.success("Patient profile retrieved", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'STAFF', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        PatientResponse response = patientService.getPatientById(user, id);
        return ResponseEntity.ok(ApiResponse.success("Patient retrieved", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<PatientResponse>>> getAllPatients(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Page<PatientResponse> response = patientService.getAllPatients(user, page, size);
        return ResponseEntity.ok(ApiResponse.success("All patients retrieved", response));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        ApiResponse<PatientResponse> response = patientService.updatePatientProfile(user, request);
        auditLogService.log(user, patientService.getPatientEntity(user), "UPDATE_PROFILE", "PATIENT_PROFILE",
                String.valueOf(response.getData().getId()), "Patient updated their profile");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/access-requests")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<java.util.List<AccessRequestResponse>>> getMyAccessRequests(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Access requests retrieved", accessRequestService.getMyRequests(user)));
    }

    @PostMapping("/access-requests")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<AccessRequestResponse>> createAccessRequest(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AccessRequestCreateRequest request) {
        AccessRequestResponse response = accessRequestService.createRequest(user, request);
        return ResponseEntity.ok(ApiResponse.success("Access request created", response));
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<java.util.List<AuditLogResponse>>> getMyAuditLogs(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", auditLogService.getPatientAuditLogs(user)));
    }
}
