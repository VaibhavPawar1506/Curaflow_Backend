package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.AdminStatsResponse;
import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.AccessRequestResponse;
import com.healthcare.management_system.dto.AccessRequestReviewRequest;
import com.healthcare.management_system.dto.AuditLogResponse;
import com.healthcare.management_system.dto.StaffResponse;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.enums.AppointmentStatus;
import com.healthcare.management_system.enums.Role;
import com.healthcare.management_system.repository.AppointmentRepository;
import com.healthcare.management_system.repository.DoctorRepository;
import com.healthcare.management_system.repository.PatientRepository;
import com.healthcare.management_system.repository.UserRepository;
import com.healthcare.management_system.service.AccessRequestService;
import com.healthcare.management_system.service.AuditLogService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Hospital admin dashboards and staff management APIs")
public class AdminController {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AccessRequestService accessRequestService;
    private final AuditLogService auditLogService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getDashboardStats(@AuthenticationPrincipal User user) {
        Hospital hospital = user.getHospital();
        long totalPatients = patientRepository.countByUser_Hospital(hospital);
        long totalDoctors = doctorRepository.countByUser_Hospital(hospital);
        long totalAppointments = appointmentRepository.countByDoctor_User_Hospital(hospital);
        long pendingAppointments = appointmentRepository.countByDoctor_User_HospitalAndStatus(
                hospital,
                AppointmentStatus.SCHEDULED);

        AdminStatsResponse stats = AdminStatsResponse.builder()
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .totalAppointments(totalAppointments)
                .pendingAppointments(pendingAppointments)
                .build();

    return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", stats));
    }

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAllStaff(@AuthenticationPrincipal User user) {
        List<User> staffUsers = userRepository.findByRoleIn(Set.of(Role.DOCTOR, Role.RECEPTIONIST)).stream()
                .filter(staffUser -> staffUser.getHospital() != null
                        && user.getHospital() != null
                        && staffUser.getHospital().getId().equals(user.getHospital().getId()))
                .collect(Collectors.toList());
        
        List<StaffResponse> staffResponses = staffUsers.stream().map(staffUser -> {
            String specialization = null;
            if (staffUser.getRole() == Role.DOCTOR) {
                specialization = doctorRepository.findByUser(staffUser)
                        .map(doctor -> doctor.getSpecialization())
                        .orElse(null);
            }
            
            return StaffResponse.builder()
                    .id(staffUser.getId())
                    .fullName(staffUser.getFullName())
                    .email(staffUser.getEmail())
                    .phone(staffUser.getPhone())
                    .role(staffUser.getRole())
                    .specialization(specialization)
                    .active(staffUser.isEnabled())
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Staff members retrieved successfully", staffResponses));
    }

    @GetMapping("/access-requests")
    public ResponseEntity<ApiResponse<List<AccessRequestResponse>>> getHospitalAccessRequests(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Access requests retrieved successfully", accessRequestService.getHospitalRequests(user)));
    }

    @PutMapping("/access-requests/{id}")
    public ResponseEntity<ApiResponse<AccessRequestResponse>> reviewAccessRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody AccessRequestReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Access request reviewed successfully", accessRequestService.reviewRequest(user, id, request)));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", auditLogService.getHospitalAuditLogs(user)));
    }
}
