package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.Lab;
import com.healthcare.management_system.entity.LabTestOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabTestOfferingRepository extends JpaRepository<LabTestOffering, Long> {
    List<LabTestOffering> findByLabOrderByCategoryAscTestNameAsc(Lab lab);
    Optional<LabTestOffering> findByLabAndTestNameIgnoreCase(Lab lab, String testName);
}
