package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.*;
import com.healthcare.management_system.entity.*;
import com.healthcare.management_system.repository.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/insurance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Insurance", description = "Insurance management APIs for claims and policies")
public class InsuranceController {

    private final InsuranceDetailsRepository detailsRepository;
    private final InsuranceClaimRepository claimRepository;
    private final PatientRepository patientRepository;
    private final BillRepository billRepository;

    // ----- PATIENT ENDPOINTS -----

    @PostMapping("/details")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<InsuranceDetails>> saveInsuranceDetails(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody InsuranceDetailsRequest request) {
        
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        InsuranceDetails details = detailsRepository.findByPatient(patient)
                .orElse(InsuranceDetails.builder().patient(patient).build());

        details.setProviderName(request.getProviderName());
        details.setPolicyNumber(request.getPolicyNumber());
        details.setGroupNumber(request.getGroupNumber());

        InsuranceDetails saved = detailsRepository.save(details);
        return ResponseEntity.ok(ApiResponse.success("Insurance details saved successfully", saved));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyInsurance(@AuthenticationPrincipal User user) {
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        Map<String, Object> response = new HashMap<>();
        
        Optional<InsuranceDetails> details = detailsRepository.findByPatient(patient);
        response.put("details", details.orElse(null));
        
        List<InsuranceClaim> claims = claimRepository.findByPatientId(patient.getId());
        response.put("claims", claims);

        return ResponseEntity.ok(ApiResponse.success("Insurance data retrieved", response));
    }

    // ----- RECEPTIONIST / ADMIN ENDPOINTS -----

    @PostMapping("/claims")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InsuranceClaim>> fileClaim(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody InsuranceClaimRequest request) {
        
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (!bill.getPatient().getUser().getHospital().getId().equals(user.getHospital().getId())) {
             throw new RuntimeException("Unauthorized: Bill belongs to another hospital");
        }

        InsuranceDetails details = detailsRepository.findByPatient(bill.getPatient())
                .orElseThrow(() -> new RuntimeException("Patient has no insurance details on file"));

        InsuranceClaim claim = InsuranceClaim.builder()
                .bill(bill)
                .patient(bill.getPatient())
                .hospital(user.getHospital())
                .providerName(details.getProviderName())
                .policyNumber(details.getPolicyNumber())
                .claimedAmount(request.getClaimedAmount())
                .status("PENDING")
                .build();

        InsuranceClaim saved = claimRepository.save(claim);
        return ResponseEntity.ok(ApiResponse.success("Claim filed successfully", saved));
    }

    @GetMapping("/claims/all")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<InsuranceClaim>>> getAllClaims(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status) {
        
        List<InsuranceClaim> claims;
        if (status != null && !status.isEmpty()) {
            claims = claimRepository.findByHospitalIdAndStatus(user.getHospital().getId(), status);
        } else {
            claims = claimRepository.findByHospitalId(user.getHospital().getId());
        }
        
        return ResponseEntity.ok(ApiResponse.success("Claims retrieved", claims));
    }

    @PutMapping("/claims/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InsuranceClaim>> processClaim(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody InsuranceClaimProcessRequest request) {
        
        InsuranceClaim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        if (!claim.getHospital().getId().equals(user.getHospital().getId())) {
            throw new RuntimeException("Unauthorized access to claim");
        }

        claim.setStatus(request.getStatus());
        claim.setRemarks(request.getRemarks());
        claim.setProcessedAt(LocalDateTime.now());

        if (request.getCoveredAmount() != null) {
            claim.setCoveredAmount(request.getCoveredAmount());
            
            // Adjust the bill if the claim was approved or partially paid
            if ("APPROVED".equals(request.getStatus()) || "PARTIALLY_PAID".equals(request.getStatus())) {
                Bill bill = claim.getBill();
                BigDecimal remaining = bill.getAmount().subtract(request.getCoveredAmount());
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    bill.setAmount(BigDecimal.ZERO);
                    bill.setStatus("PAID");
                    bill.setPaidAt(LocalDateTime.now());
                } else {
                    bill.setAmount(remaining);
                }
                billRepository.save(bill);
            }
        }

        InsuranceClaim saved = claimRepository.save(claim);
        return ResponseEntity.ok(ApiResponse.success("Claim processed successfully", saved));
    }
}
