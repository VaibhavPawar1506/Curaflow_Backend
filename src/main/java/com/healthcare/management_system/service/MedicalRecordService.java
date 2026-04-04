package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.MedicalRecordRequest;
import com.healthcare.management_system.dto.MedicalRecordResponse;
import com.healthcare.management_system.entity.Doctor;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.MedicalRecord;
import com.healthcare.management_system.entity.Patient;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.exception.BadRequestException;
import com.healthcare.management_system.exception.ResourceNotFoundException;
import com.healthcare.management_system.repository.DoctorRepository;
import com.healthcare.management_system.repository.MedicalRecordRepository;
import com.healthcare.management_system.repository.PatientRepository;
import com.healthcare.management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public ApiResponse<MedicalRecordResponse> addRecord(Long patientId, MedicalRecordRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String currentUserRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
        User currentUser = getCurrentUser(currentUserEmail);
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));
        validateHospitalAccess(currentUser, patient);

        MedicalRecord.MedicalRecordBuilder recordBuilder = MedicalRecord.builder()
                .patient(patient)
                .diagnosis(request.getDiagnosis())
                .symptoms(request.getSymptoms())
                .prescription(request.getPrescription())
                .notes(request.getNotes())
                .weight(request.getWeight())
                .height(request.getHeight())
                .bloodGroup(request.getBloodGroup())
                .bloodPressure(request.getBloodPressure())
                .heartbeat(request.getHeartbeat())
                .bloodOxygen(request.getBloodOxygen())
                .prescriptionFile(request.getPrescriptionFile());

        if (currentUserRole.equals("ROLE_DOCTOR")) {
            Doctor doctor = doctorRepository.findByUser_Email(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
            recordBuilder.doctor(doctor);
        } else if (request.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));
            validateDoctorHospitalAccess(currentUser, doctor);
            recordBuilder.doctor(doctor);
        }
        
        // Add prescription file if provided in request (we'll assume the DTO has it or handle it separately)
        // For now, let's add prescriptionFile to MedicalRecordRequest.

        MedicalRecord savedRecord = medicalRecordRepository.save(recordBuilder.build());
        return ApiResponse.success("Medical record added successfully", mapToResponse(savedRecord));
    }

    public ApiResponse<List<MedicalRecordResponse>> getPatientRecords(Long patientId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String currentUserRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
        User currentUser = getCurrentUser(currentUserEmail);
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

        // Security check: If the user is a patient, they can only view their own records
        if (currentUserRole.equals("ROLE_PATIENT")) {
            if (!patient.getUser().getEmail().equals(currentUserEmail)) {
                throw new BadRequestException("You are not authorized to view these medical records");
            }
        } else {
            validateHospitalAccess(currentUser, patient);
        }

        List<MedicalRecordResponse> records = medicalRecordRepository.findByPatientOrderByRecordDateDesc(patient)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Medical records retrieved successfully", records);
    }
    
    public ApiResponse<MedicalRecordResponse> getRecordById(Long recordId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String currentUserRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
        User currentUser = getCurrentUser(currentUserEmail);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with ID: " + recordId));

        if (currentUserRole.equals("ROLE_PATIENT")) {
            if (!record.getPatient().getUser().getEmail().equals(currentUserEmail)) {
                throw new BadRequestException("You are not authorized to view this medical record");
            }
        } else {
            validateHospitalAccess(currentUser, record.getPatient());
        }

        return ApiResponse.success("Medical record retrieved successfully", mapToResponse(record));
    }

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .patientId(record.getPatient().getId())
                .patientName(record.getPatient().getUser().getFullName())
                .doctorId(record.getDoctor() != null ? record.getDoctor().getId() : null)
                .doctorName(record.getDoctor() != null ? record.getDoctor().getUser().getFullName() : "N/A")
                .hospitalId(record.getDoctor() != null
                        && record.getDoctor().getUser() != null
                        && record.getDoctor().getUser().getHospital() != null
                        ? record.getDoctor().getUser().getHospital().getId()
                        : null)
                .hospitalName(record.getDoctor() != null
                        && record.getDoctor().getUser() != null
                        && record.getDoctor().getUser().getHospital() != null
                        ? record.getDoctor().getUser().getHospital().getName()
                        : "Hospital not available")
                .diagnosis(record.getDiagnosis())
                .symptoms(record.getSymptoms())
                .prescription(record.getPrescription())
                .notes(record.getNotes())
                .weight(record.getWeight())
                .height(record.getHeight())
                .bloodGroup(record.getBloodGroup())
                .bloodPressure(record.getBloodPressure())
                .heartbeat(record.getHeartbeat())
                .bloodOxygen(record.getBloodOxygen())
                .prescriptionFile(record.getPrescriptionFile())
                .recordDate(record.getRecordDate())
                .createdAt(record.getCreatedAt())
                .build();
    }

    private User getCurrentUser(String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private void validateHospitalAccess(User currentUser, Patient patient) {
        Hospital currentHospital = currentUser.getHospital();
        Hospital patientHospital = patient.getUser().getHospital();

        if (currentHospital == null || patientHospital == null || !currentHospital.getId().equals(patientHospital.getId())) {
            throw new BadRequestException("You are not authorized to access records from another hospital");
        }
    }

    private void validateDoctorHospitalAccess(User currentUser, Doctor doctor) {
        Hospital currentHospital = currentUser.getHospital();
        Hospital doctorHospital = doctor.getUser().getHospital();

        if (currentHospital == null || doctorHospital == null || !currentHospital.getId().equals(doctorHospital.getId())) {
            throw new BadRequestException("You are not authorized to assign a doctor from another hospital");
        }
    }
}
