package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.LabReport;
import com.healthcare.management_system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    List<LabReport> findByPatientOrderByReportDateDesc(Patient patient);
}
