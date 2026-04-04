package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.Appointment;
import com.healthcare.management_system.entity.Doctor;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientOrderByAppointmentDateTimeDesc(Patient patient);
    List<Appointment> findByDoctorOrderByAppointmentDateTimeDesc(Doctor doctor);
    List<Appointment> findByDoctor_User_HospitalOrderByAppointmentDateTimeDesc(Hospital hospital);
    long countByDoctor_User_Hospital(Hospital hospital);
    long countByDoctor_User_HospitalAndStatus(
            Hospital hospital,
            com.healthcare.management_system.enums.AppointmentStatus status);
    List<Appointment> findByDoctorAndAppointmentDateTimeBetween(
            Doctor doctor, LocalDateTime start, LocalDateTime end);
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Appointment a WHERE " +
            "(:hospitalId IS NULL OR a.doctor.user.hospital.id = :hospitalId) AND " +
            "(:doctorId IS NULL OR a.doctor.id = :doctorId) AND " +
            "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(cast(:startDate as java.time.LocalDateTime) IS NULL OR a.appointmentDateTime >= :startDate) AND " +
            "(cast(:endDate as java.time.LocalDateTime) IS NULL OR a.appointmentDateTime <= :endDate)")
    org.springframework.data.domain.Page<Appointment> filterAppointments(
            @org.springframework.data.repository.query.Param("hospitalId") Long hospitalId,
            @org.springframework.data.repository.query.Param("doctorId") Long doctorId,
            @org.springframework.data.repository.query.Param("patientId") Long patientId,
            @org.springframework.data.repository.query.Param("status") com.healthcare.management_system.enums.AppointmentStatus status,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable);
}
