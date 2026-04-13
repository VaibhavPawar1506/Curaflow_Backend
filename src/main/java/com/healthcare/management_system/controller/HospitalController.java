package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.HospitalDirectoryResponse;
import com.healthcare.management_system.service.HospitalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Hospitals", description = "Hospital directory APIs for discovery and booking")
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'RECEPTIONIST', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<HospitalDirectoryResponse>>> getApprovedHospitals() {
        return ResponseEntity.ok(ApiResponse.success("Hospitals retrieved successfully", hospitalService.getApprovedHospitals()));
    }
}
