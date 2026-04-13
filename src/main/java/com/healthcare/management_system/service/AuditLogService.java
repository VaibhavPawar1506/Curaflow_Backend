package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.AuditLogResponse;
import com.healthcare.management_system.entity.AuditLog;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.Patient;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.exception.BadRequestException;
import com.healthcare.management_system.exception.ResourceNotFoundException;
import com.healthcare.management_system.repository.AuditLogRepository;
import com.healthcare.management_system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;

    public void log(User actor, Patient patient, String action, String targetType, String targetId, String details) {
        Hospital hospital = actor != null ? actor.getHospital() : null;
        AuditLog auditLog = AuditLog.builder()
                .user(actor)
                .hospital(hospital)
                .patient(patient)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
    }

    public List<AuditLogResponse> getPatientAuditLogs(User user) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        return auditLogRepository.findTop20ByPatientOrderByCreatedAtDesc(patient)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AuditLogResponse> getHospitalAuditLogs(User user) {
        if (user.getHospital() == null) {
            throw new BadRequestException("User is not associated with a hospital");
        }
        return auditLogRepository.findTop50ByHospitalOrderByCreatedAtDesc(user.getHospital())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .userName(auditLog.getUser() != null ? auditLog.getUser().getFullName() : "System")
                .patientId(auditLog.getPatient() != null ? auditLog.getPatient().getId() : null)
                .patientName(auditLog.getPatient() != null ? auditLog.getPatient().getUser().getFullName() : null)
                .action(auditLog.getAction())
                .targetType(auditLog.getTargetType())
                .targetId(auditLog.getTargetId())
                .details(auditLog.getDetails())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
