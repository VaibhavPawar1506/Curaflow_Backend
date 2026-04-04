package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.DepartmentDirectoryResponse;
import com.healthcare.management_system.entity.Department;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.enums.HospitalStatus;
import com.healthcare.management_system.enums.Role;
import com.healthcare.management_system.exception.ResourceNotFoundException;
import com.healthcare.management_system.repository.DepartmentRepository;
import com.healthcare.management_system.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;

    private static final List<String> DEFAULT_DEPARTMENTS = Arrays.asList(
            "Cardiology", "Oncology", "Eyecare", "Pediatrics", "Neurology",
            "Orthopedics", "Radiology", "Dental", "ENT", "Gastroenterology"
    );

    @Transactional
    public void seedDefaultDepartments(User currentUser, Long hospitalId) {
        Hospital hospital = getAuthorizedHospital(currentUser, hospitalId);

        if (departmentRepository.findByHospitalId(hospitalId).isEmpty()) {
            for (String deptName : DEFAULT_DEPARTMENTS) {
                Department dept = Department.builder()
                        .name(deptName)
                        .description("Default " + deptName + " department.")
                        .hospital(hospital)
                        .active(true)
                        .build();
                departmentRepository.save(dept);
            }
        }
    }

    public List<Department> getHospitalDepartments(User currentUser, Long hospitalId) {
        Hospital hospital = getAuthorizedHospital(currentUser, hospitalId);
        return departmentRepository.findByHospital(hospital);
    }

    public List<DepartmentDirectoryResponse> getHospitalDepartmentDirectory(User currentUser, Long hospitalId) {
        Hospital hospital = getAuthorizedHospital(currentUser, hospitalId);
        return departmentRepository.findByHospitalIdAndActiveTrueOrderByNameAsc(hospital.getId()).stream()
                .map(this::mapToDirectory)
                .toList();
    }

    @Transactional
    public Department updateDepartmentStatus(User currentUser, Long departmentId, boolean active) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        validateSameHospital(currentUser, department.getHospital());
        department.setActive(active);
        return departmentRepository.save(department);
    }

    private Hospital getAuthorizedHospital(User currentUser, Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        if (currentUser != null && currentUser.getRole() == Role.PATIENT) {
            if (hospital.getStatus() != HospitalStatus.APPROVED) {
                throw new AccessDeniedException("Only approved hospitals are available for appointment booking");
            }
            return hospital;
        }
        validateSameHospital(currentUser, hospital);
        return hospital;
    }

    private void validateSameHospital(User currentUser, Hospital targetHospital) {
        if (currentUser == null) {
            throw new AccessDeniedException("Access denied");
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return;
        }
        if (currentUser.getHospital() == null || targetHospital == null
                || !currentUser.getHospital().getId().equals(targetHospital.getId())) {
            throw new AccessDeniedException("You are not authorized to access another hospital's departments");
        }
    }

    private DepartmentDirectoryResponse mapToDirectory(Department department) {
        return DepartmentDirectoryResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .active(department.isActive())
                .hospitalId(department.getHospital().getId())
                .build();
    }
}
