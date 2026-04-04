package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.AccessRequestCreateRequest;
import com.healthcare.management_system.dto.AccessRequestResponse;
import com.healthcare.management_system.dto.AccessRequestReviewRequest;
import com.healthcare.management_system.entity.AccessRequest;
import com.healthcare.management_system.entity.Patient;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.enums.AccessRequestStatus;
import com.healthcare.management_system.exception.BadRequestException;
import com.healthcare.management_system.exception.ResourceNotFoundException;
import com.healthcare.management_system.repository.AccessRequestRepository;
import com.healthcare.management_system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessRequestService {

    private final AccessRequestRepository accessRequestRepository;
    private final PatientRepository patientRepository;
    private final AuditLogService auditLogService;

    public AccessRequestResponse createRequest(User user, AccessRequestCreateRequest request) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        AccessRequest accessRequest = AccessRequest.builder()
                .patient(patient)
                .requestedHospitalName(request.getRequestedHospitalName().trim())
                .requestedHospitalCode(request.getRequestedHospitalCode())
                .purpose(request.getPurpose().trim())
                .notes(request.getNotes())
                .status(AccessRequestStatus.PENDING)
                .build();

        AccessRequest savedRequest = accessRequestRepository.save(accessRequest);
        auditLogService.log(user, patient, "CREATE_ACCESS_REQUEST", "ACCESS_REQUEST", String.valueOf(savedRequest.getId()),
                "Requested record sharing with " + savedRequest.getRequestedHospitalName());
        return mapToResponse(savedRequest);
    }

    public List<AccessRequestResponse> getMyRequests(User user) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        return accessRequestRepository.findByPatientOrderByCreatedAtDesc(patient)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AccessRequestResponse> getHospitalRequests(User user) {
        if (user.getHospital() == null) {
            throw new BadRequestException("User is not associated with a hospital");
        }
        return accessRequestRepository.findByPatient_User_HospitalOrderByCreatedAtDesc(user.getHospital())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AccessRequestResponse reviewRequest(User user, Long requestId, AccessRequestReviewRequest request) {
        AccessRequest accessRequest = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Access request not found"));

        if (user.getHospital() == null
                || accessRequest.getPatient().getUser().getHospital() == null
                || !user.getHospital().getId().equals(accessRequest.getPatient().getUser().getHospital().getId())) {
            throw new ResourceNotFoundException("Access request not found");
        }

        if (request.getStatus() == AccessRequestStatus.PENDING) {
            throw new BadRequestException("Request review must be approved or rejected");
        }

        accessRequest.setStatus(request.getStatus());
        accessRequest.setNotes(request.getNotes());
        accessRequest.setReviewedBy(user);
        accessRequest.setReviewedAt(LocalDateTime.now());

        AccessRequest savedRequest = accessRequestRepository.save(accessRequest);
        auditLogService.log(user, savedRequest.getPatient(), "REVIEW_ACCESS_REQUEST", "ACCESS_REQUEST",
                String.valueOf(savedRequest.getId()),
                "Marked access request as " + savedRequest.getStatus());
        return mapToResponse(savedRequest);
    }

    private AccessRequestResponse mapToResponse(AccessRequest accessRequest) {
        return AccessRequestResponse.builder()
                .id(accessRequest.getId())
                .patientId(accessRequest.getPatient().getId())
                .patientName(accessRequest.getPatient().getUser().getFullName())
                .requestedHospitalName(accessRequest.getRequestedHospitalName())
                .requestedHospitalCode(accessRequest.getRequestedHospitalCode())
                .purpose(accessRequest.getPurpose())
                .status(accessRequest.getStatus())
                .notes(accessRequest.getNotes())
                .reviewedByName(accessRequest.getReviewedBy() != null ? accessRequest.getReviewedBy().getFullName() : null)
                .createdAt(accessRequest.getCreatedAt())
                .reviewedAt(accessRequest.getReviewedAt())
                .build();
    }
}
