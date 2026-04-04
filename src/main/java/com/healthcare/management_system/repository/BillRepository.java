package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.Bill;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPatientOrderByCreatedAtDesc(Patient patient);
    List<Bill> findByPatient_User_HospitalOrderByCreatedAtDesc(Hospital hospital);
}
