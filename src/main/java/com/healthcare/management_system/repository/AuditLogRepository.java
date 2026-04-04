package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.AuditLog;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop20ByPatientOrderByCreatedAtDesc(Patient patient);
    List<AuditLog> findTop50ByHospitalOrderByCreatedAtDesc(Hospital hospital);
}
