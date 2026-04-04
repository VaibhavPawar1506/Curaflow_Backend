package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.DoctorResponse;
import com.healthcare.management_system.entity.Department;
import com.healthcare.management_system.entity.Doctor;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.exception.ResourceNotFoundException;
import com.healthcare.management_system.repository.DoctorRepository;
import com.healthcare.management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public DoctorResponse getDoctorProfile(User user) {
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        return mapToResponse(doctor);
    }

    public DoctorResponse getDoctorById(User currentUser, Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        validateHospitalAccess(currentUser, doctor);
        return mapToResponse(doctor);
    }

    public org.springframework.data.domain.Page<DoctorResponse> getAllDoctors(User currentUser, int page, int size) {
        return getAllDoctors(currentUser, null, null, page, size);
    }

    public org.springframework.data.domain.Page<DoctorResponse> getAllDoctors(User currentUser, Long requestedHospitalId, Long requestedDepartmentId, int page, int size) {
        Long hospitalId = getScopedHospitalId(currentUser, requestedHospitalId);
        Long departmentId = getScopedDepartmentId(currentUser, requestedDepartmentId);
        return doctorRepository.searchDoctors(hospitalId, departmentId, null, null, null, org.springframework.data.domain.PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    public org.springframework.data.domain.Page<DoctorResponse> searchDoctors(String specialization, Double maxFee, String availableTimeStr, int page, int size) {
        return searchDoctors(null, null, null, specialization, maxFee, availableTimeStr, page, size);
    }

    public org.springframework.data.domain.Page<DoctorResponse> searchDoctors(User user, String specialization, Double maxFee, String availableTimeStr, int page, int size) {
        return searchDoctors(user, null, null, specialization, maxFee, availableTimeStr, page, size);
    }

    public org.springframework.data.domain.Page<DoctorResponse> searchDoctors(
            User user,
            Long requestedHospitalId,
            Long requestedDepartmentId,
            String specialization,
            Double maxFee,
            String availableTimeStr,
            int page,
            int size) {
        java.math.BigDecimal fee = maxFee != null ? java.math.BigDecimal.valueOf(maxFee) : null;
        java.time.LocalTime time = availableTimeStr != null ? java.time.LocalTime.parse(availableTimeStr) : null;
        Long hospitalId = getScopedHospitalId(user, requestedHospitalId);
        Long departmentId = getScopedDepartmentId(user, requestedDepartmentId);
        return doctorRepository.searchDoctors(hospitalId, departmentId, specialization, fee, time, org.springframework.data.domain.PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    public com.healthcare.management_system.dto.ApiResponse<DoctorResponse> updateDoctorProfile(User user, com.healthcare.management_system.dto.UserProfileUpdateRequest request) {
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        
        if (request.getSpecialization() != null) doctor.setSpecialization(request.getSpecialization());
        if (request.getLicenseNumber() != null) doctor.setLicenseNumber(request.getLicenseNumber());
        if (request.getExperienceYears() != null) doctor.setExperienceYears(request.getExperienceYears());
        if (request.getConsultationFee() != null) doctor.setConsultationFee(java.math.BigDecimal.valueOf(request.getConsultationFee()));
        if (request.getAvailableFrom() != null) doctor.setAvailableFrom(java.time.LocalTime.parse(request.getAvailableFrom()));
        if (request.getAvailableTo() != null) doctor.setAvailableTo(java.time.LocalTime.parse(request.getAvailableTo()));

        userRepository.save(user);
        Doctor updatedDoctor = doctorRepository.save(doctor);
        return com.healthcare.management_system.dto.ApiResponse.success("Profile updated successfully", mapToResponse(updatedDoctor));
    }

    private Long getScopedHospitalId(User user, Long requestedHospitalId) {
        if (user == null) {
            return requestedHospitalId;
        }
        if (user.getRole() == com.healthcare.management_system.enums.Role.PATIENT || user.getRole() == com.healthcare.management_system.enums.Role.SUPER_ADMIN) {
            return requestedHospitalId;
        }
        if (user.getHospital() == null) {
            return null;
        }
        return user.getHospital().getId();
    }

    private Long getScopedDepartmentId(User user, Long requestedDepartmentId) {
        if (user == null) {
            return requestedDepartmentId;
        }
        return user.getRole() == com.healthcare.management_system.enums.Role.PATIENT
                || user.getRole() == com.healthcare.management_system.enums.Role.SUPER_ADMIN
                ? requestedDepartmentId
                : requestedDepartmentId;
    }

    private void validateHospitalAccess(User currentUser, Doctor doctor) {
        if (currentUser == null
                || currentUser.getRole() == com.healthcare.management_system.enums.Role.SUPER_ADMIN
                || currentUser.getRole() == com.healthcare.management_system.enums.Role.PATIENT) {
            return;
        }

        Hospital currentHospital = currentUser.getHospital();
        Hospital doctorHospital = doctor.getUser().getHospital();
        if (currentHospital == null || doctorHospital == null || !currentHospital.getId().equals(doctorHospital.getId())) {
            throw new AccessDeniedException("You are not authorized to access doctors from another hospital");
        }
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
        Hospital hospital = doctor.getUser().getHospital();
        Department department = doctor.getDepartment();
        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(doctor.getUser().getId())
                .fullName(doctor.getUser().getFullName())
                .email(doctor.getUser().getEmail())
                .phone(doctor.getUser().getPhone())
                .hospitalId(hospital != null ? hospital.getId() : null)
                .hospitalName(hospital != null ? hospital.getName() : null)
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)
                .specialization(doctor.getSpecialization())
                .licenseNumber(doctor.getLicenseNumber())
                .experienceYears(doctor.getExperienceYears())
                .consultationFee(doctor.getConsultationFee())
                .availableFrom(doctor.getAvailableFrom())
                .availableTo(doctor.getAvailableTo())
                .build();
    }
}
