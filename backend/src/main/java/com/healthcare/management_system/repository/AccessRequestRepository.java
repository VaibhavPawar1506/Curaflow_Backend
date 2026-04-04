package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.AccessRequest;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
    List<AccessRequest> findByPatientOrderByCreatedAtDesc(Patient patient);
    List<AccessRequest> findByPatient_User_HospitalOrderByCreatedAtDesc(Hospital hospital);
}
