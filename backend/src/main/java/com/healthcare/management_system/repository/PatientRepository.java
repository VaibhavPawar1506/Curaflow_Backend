package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.Patient;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser(User user);
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByUser_Email(String email);
    Optional<Patient> findByUser_HospitalAndId(Hospital hospital, Long id);
    long countByUser_Hospital(Hospital hospital);
    org.springframework.data.domain.Page<Patient> findByUser_Hospital(
            Hospital hospital,
            org.springframework.data.domain.Pageable pageable);
}
