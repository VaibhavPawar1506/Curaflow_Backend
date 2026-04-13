package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.Department;
import com.healthcare.management_system.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByHospital(Hospital hospital);
    List<Department> findByHospitalId(Long hospitalId);
    List<Department> findByHospitalIdAndActiveTrueOrderByNameAsc(Long hospitalId);
    Optional<Department> findByHospitalIdAndNameIgnoreCase(Long hospitalId, String name);
}
