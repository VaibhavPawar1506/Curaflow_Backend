package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.ApprovalActionRequest;
import com.healthcare.management_system.dto.PhysicalVerificationRequest;
import com.healthcare.management_system.dto.ScheduleInspectionRequest;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.enums.HospitalStatus;
import com.healthcare.management_system.exception.BadRequestException;
import com.healthcare.management_system.exception.ResourceNotFoundException;
import com.healthcare.management_system.repository.HospitalRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Super Admin", description = "Platform-level hospital review, physical verification, and approval APIs")
public class SuperAdminController {

    private final HospitalRepository hospitalRepository;

    @GetMapping("/hospitals/review-queue")
    public ResponseEntity<ApiResponse<List<Hospital>>> getReviewQueue() {
        return ResponseEntity.ok(ApiResponse.success("Hospital review queue retrieved",
                hospitalRepository.findAll().stream()
                        .filter(h -> h.getStatus() != HospitalStatus.APPROVED)
                        .toList()));
    }

    @PutMapping("/hospitals/{id}/schedule-inspection")
    public ResponseEntity<ApiResponse<Hospital>> scheduleInspection(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleInspectionRequest request) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        hospital.setStatus(HospitalStatus.INSPECTION_SCHEDULED);
        hospital.setVerificationOfficer(request.getOfficer());
        hospital.setInspectionScheduledAt(request.getScheduledAt());
        hospital.setVerificationNotes(request.getNotes());
        hospital.setLocationVerified(Boolean.FALSE);
        return ResponseEntity.ok(ApiResponse.success("Physical inspection scheduled", hospitalRepository.save(hospital)));
    }

    @PutMapping("/hospitals/{id}/physical-verify")
    public ResponseEntity<ApiResponse<Hospital>> completePhysicalVerification(
            @PathVariable Long id,
            @Valid @RequestBody PhysicalVerificationRequest request) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        hospital.setVerificationOfficer(request.getOfficer());
        hospital.setLocationVerified(request.isLocationVerified());
        hospital.setPhysicallyVerifiedAt(LocalDateTime.now());
        hospital.setVerificationNotes(request.getNotes());
        hospital.setStatus(request.isLocationVerified() ? HospitalStatus.APPROVED : HospitalStatus.REJECTED);
        return ResponseEntity.ok(ApiResponse.success(
                request.isLocationVerified() ? "Hospital physically verified and approved" : "Hospital rejected after physical verification",
                hospitalRepository.save(hospital)));
    }

    @PutMapping("/hospitals/{id}/approve")
    public ResponseEntity<ApiResponse<Hospital>> approveHospital(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionRequest request) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        if (hospital.getStatus() != HospitalStatus.PHYSICALLY_VERIFIED) {
            throw new BadRequestException("Hospital must be physically verified before approval");
        }
        hospital.setStatus(HospitalStatus.APPROVED);
        hospital.setVerificationNotes(request.getNotes());
        return ResponseEntity.ok(ApiResponse.success("Hospital approved successfully", hospitalRepository.save(hospital)));
    }

    @PutMapping("/hospitals/{id}/reject")
    public ResponseEntity<ApiResponse<Hospital>> rejectHospital(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionRequest request) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        hospital.setStatus(HospitalStatus.REJECTED);
        hospital.setVerificationNotes(request.getNotes());
        return ResponseEntity.ok(ApiResponse.success("Hospital rejected", hospitalRepository.save(hospital)));
    }
}
