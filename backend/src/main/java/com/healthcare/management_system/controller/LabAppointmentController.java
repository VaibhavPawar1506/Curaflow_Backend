package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.LabAppointmentCreateRequest;
import com.healthcare.management_system.dto.LabAppointmentResponse;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.service.LabService;
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
@RequestMapping("/api/lab-appointments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Lab Appointments", description = "Patient lab test bookings")
public class LabAppointmentController {

    private final LabService labService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<LabAppointmentResponse>> createAppointment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody LabAppointmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lab appointment booked successfully", labService.bookAppointment(user, request)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<LabAppointmentResponse>>> getMyAppointments(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Lab appointments retrieved successfully", labService.getMyAppointments(user)));
    }
}
