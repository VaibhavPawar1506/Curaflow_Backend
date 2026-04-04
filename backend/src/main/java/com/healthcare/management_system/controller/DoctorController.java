package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.DoctorResponse;
import com.healthcare.management_system.service.DoctorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.healthcare.management_system.dto.UserProfileUpdateRequest;
import com.healthcare.management_system.entity.User;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Doctors", description = "Doctor directory, profile, and search APIs")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'RECEPTIONIST', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<DoctorResponse>>> getAllDoctors(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved successfully", doctorService.getAllDoctors(user, hospitalId, departmentId, page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'RECEPTIONIST', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        DoctorResponse response = doctorService.getDoctorById(user, id);
        return ResponseEntity.ok(ApiResponse.success("Doctor retrieved", response));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorResponse>> getMyProfile(@AuthenticationPrincipal User user) {
        DoctorResponse response = doctorService.getDoctorProfile(user);
        return ResponseEntity.ok(ApiResponse.success("Doctor profile retrieved", response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'RECEPTIONIST', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<DoctorResponse>>> searchDoctors(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Double maxFee,
            @RequestParam(required = false) String availableTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved successfully", doctorService.searchDoctors(user, hospitalId, departmentId, specialization, maxFee, availableTime, page, size)));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(doctorService.updateDoctorProfile(user, request));
    }
}
