package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.AppointmentRequest;
import com.healthcare.management_system.dto.AppointmentRescheduleRequest;
import com.healthcare.management_system.dto.AppointmentResponse;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.service.AppointmentService;
import com.healthcare.management_system.service.AuditLogService;
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
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Appointments", description = "Appointment booking and hospital-scoped appointment workflows")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuditLogService auditLogService;
    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.bookAppointment(user, request);
        auditLogService.log(user, patientService.getPatientEntity(user), "BOOK_APPOINTMENT", "APPOINTMENT",
                String.valueOf(response.getId()), "Booked appointment with Dr. " + response.getDoctorName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment booked successfully", response));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        AppointmentResponse response = appointmentService.cancelAppointment(id, user);
        if ("PATIENT".equals(user.getRole().name())) {
            auditLogService.log(user, patientService.getPatientEntity(user), "CANCEL_APPOINTMENT", "APPOINTMENT",
                    String.valueOf(response.getId()), "Cancelled appointment with Dr. " + response.getDoctorName());
        }
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", response));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AppointmentRescheduleRequest request) {
        AppointmentResponse response = appointmentService.rescheduleAppointment(id, user, request);
        if ("PATIENT".equals(user.getRole().name())) {
            auditLogService.log(user, patientService.getPatientEntity(user), "RESCHEDULE_APPOINTMENT", "APPOINTMENT",
                    String.valueOf(response.getId()), "Rescheduled appointment with Dr. " + response.getDoctorName());
        }
        return ResponseEntity.ok(ApiResponse.success("Appointment rescheduled successfully", response));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        AppointmentResponse response = appointmentService.completeAppointment(id, user);
        return ResponseEntity.ok(ApiResponse.success("Appointment completed successfully", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMyAppointments(
            @AuthenticationPrincipal User user) {
        List<AppointmentResponse> response = appointmentService.getMyAppointments(user);
        if ("PATIENT".equals(user.getRole().name())) {
            auditLogService.log(user, patientService.getPatientEntity(user), "VIEW_APPOINTMENTS", "APPOINTMENT_LIST",
                    null, "Patient viewed their appointments");
        }
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved", response));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<AppointmentResponse>>> filterAppointments(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) com.healthcare.management_system.enums.AppointmentStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponse.success("Appointments filtered successfully", appointmentService.filterMyAppointments(user, status, startDate, endDate, page, size)));
    }
}
