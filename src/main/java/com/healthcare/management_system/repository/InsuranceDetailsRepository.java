package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.InsuranceDetails;
import com.healthcare.management_system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InsuranceDetailsRepository extends JpaRepository<InsuranceDetails, Long> {
    Optional<InsuranceDetails> findByPatient(Patient patient);
    Optional<InsuranceDetails> findByPatientId(Long patientId);
}
