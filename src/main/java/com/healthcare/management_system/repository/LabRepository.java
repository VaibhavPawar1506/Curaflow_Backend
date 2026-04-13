package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabRepository extends JpaRepository<Lab, Long> {
    List<Lab> findByActiveTrueOrderByNameAsc();
    Optional<Lab> findByLabCode(String labCode);
}
