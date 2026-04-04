package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.LabAppointment;
import com.healthcare.management_system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabAppointmentRepository extends JpaRepository<LabAppointment, Long> {
    List<LabAppointment> findByPatientOrderByAppointmentDateTimeDesc(Patient patient);
}
