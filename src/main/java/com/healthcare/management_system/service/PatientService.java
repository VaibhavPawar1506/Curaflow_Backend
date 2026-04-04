package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.PatientResponse;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.Patient;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.exception.BadRequestException;
import com.healthcare.management_system.exception.ResourceNotFoundException;
import com.healthcare.management_system.repository.PatientRepository;
import com.healthcare.management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientResponse getPatientProfile(User user) {
        Patient patient = getPatientEntity(user);
        return mapToResponse(patient);
    }

    public Patient getPatientEntity(User user) {
        return patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
    }

    public PatientResponse getPatientById(User currentUser, Long id) {
        Hospital hospital = requireHospital(currentUser);
        Patient patient = patientRepository.findByUser_HospitalAndId(hospital, id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return mapToResponse(patient);
    }

    public org.springframework.data.domain.Page<PatientResponse> getAllPatients(User currentUser, int page, int size) {
        Hospital hospital = requireHospital(currentUser);
        return patientRepository.findByUser_Hospital(hospital, org.springframework.data.domain.PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    public com.healthcare.management_system.dto.ApiResponse<PatientResponse> updatePatientProfile(User user, com.healthcare.management_system.dto.UserProfileUpdateRequest request) {
        Patient patient = getPatientEntity(user);

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        
        if (request.getBloodGroup() != null) patient.setBloodGroup(request.getBloodGroup());
        if (request.getAddress() != null) patient.setAddress(request.getAddress());
        if (request.getEmergencyContact() != null) patient.setEmergencyContact(request.getEmergencyContact());

        userRepository.save(user);
        Patient updatedPatient = patientRepository.save(patient);
        return com.healthcare.management_system.dto.ApiResponse.success("Profile updated successfully", mapToResponse(updatedPatient));
    }

    private PatientResponse mapToResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .userId(patient.getUser().getId())
                .fullName(patient.getUser().getFullName())
                .email(patient.getUser().getEmail())
                .phone(patient.getUser().getPhone())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .bloodGroup(patient.getBloodGroup())
                .address(patient.getAddress())
                .emergencyContact(patient.getEmergencyContact())
                .build();
    }

    private Hospital requireHospital(User currentUser) {
        if (currentUser == null || currentUser.getHospital() == null) {
            throw new BadRequestException("User is not associated with a hospital");
        }
        return currentUser.getHospital();
    }
}
