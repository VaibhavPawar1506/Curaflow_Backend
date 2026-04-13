package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.MedicalRecord;
import com.healthcare.management_system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientOrderByRecordDateDesc(Patient patient);
}
