package com.healthcare.management_system.repository;

import com.healthcare.management_system.entity.Doctor;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser(User user);
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUser_Email(String email);
    long countByUser_Hospital(Hospital hospital);
    List<Doctor> findByUser_Hospital(Hospital hospital);
    @org.springframework.data.jpa.repository.Query("SELECT d FROM Doctor d WHERE " +
            "(:hospitalId IS NULL OR d.user.hospital.id = :hospitalId) AND " +
            "(:departmentId IS NULL OR d.department.id = :departmentId) AND " +
            "(:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) AND " +
            "(:maxFee IS NULL OR d.consultationFee <= :maxFee) AND " +
            "(:availableTime IS NULL OR (d.availableFrom <= :availableTime AND d.availableTo >= :availableTime))")
    org.springframework.data.domain.Page<Doctor> searchDoctors(
            @org.springframework.data.repository.query.Param("hospitalId") Long hospitalId,
            @org.springframework.data.repository.query.Param("departmentId") Long departmentId,
            @org.springframework.data.repository.query.Param("specialization") String specialization,
            @org.springframework.data.repository.query.Param("maxFee") java.math.BigDecimal maxFee,
            @org.springframework.data.repository.query.Param("availableTime") java.time.LocalTime availableTime,
            org.springframework.data.domain.Pageable pageable);
}
