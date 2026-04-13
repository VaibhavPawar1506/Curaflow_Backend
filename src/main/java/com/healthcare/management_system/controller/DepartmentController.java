package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.DepartmentDirectoryResponse;
import com.healthcare.management_system.dto.DepartmentStatusUpdateRequest;
import com.healthcare.management_system.entity.Department;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.service.DepartmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Departments", description = "Hospital department listing and administration APIs")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'RECEPTIONIST', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments(
            @AuthenticationPrincipal User user,
            @PathVariable Long hospitalId) {
        return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", 
                departmentService.getHospitalDepartments(user, hospitalId)));
    }

    @GetMapping("/hospital/{hospitalId}/directory")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'RECEPTIONIST', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentDirectoryResponse>>> getDepartmentDirectory(
            @AuthenticationPrincipal User user,
            @PathVariable Long hospitalId) {
        return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully",
                departmentService.getHospitalDepartmentDirectory(user, hospitalId)));
    }

    @PostMapping("/seed/{hospitalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> seedDepartments(
            @AuthenticationPrincipal User user,
            @PathVariable Long hospitalId) {
        departmentService.seedDefaultDepartments(user, hospitalId);
        return ResponseEntity.ok(ApiResponse.success("Default departments seeded successfully", null));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Department>> updateStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody DepartmentStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Department status updated", 
                departmentService.updateDepartmentStatus(user, id, request.isActive())));
    }
}
