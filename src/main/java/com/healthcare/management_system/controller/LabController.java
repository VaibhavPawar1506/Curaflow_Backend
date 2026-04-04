package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.LabDirectoryResponse;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.service.LabService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/labs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Labs", description = "Lab directory and patient lab workflows")
public class LabController {

    private final LabService labService;

    @GetMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<LabDirectoryResponse>>> getLabs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Labs retrieved successfully", labService.getActiveLabs()));
    }
}
