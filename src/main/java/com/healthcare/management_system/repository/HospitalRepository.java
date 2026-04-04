package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.enums.HospitalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByHospitalCode(String hospitalCode);
    List<Hospital> findByStatusOrderByNameAsc(HospitalStatus status);
}
