package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {
    List<InsuranceClaim> findByPatientId(Long patientId);
    List<InsuranceClaim> findByHospitalId(Long hospitalId);
    List<InsuranceClaim> findByHospitalIdAndStatus(Long hospitalId, String status);
}
