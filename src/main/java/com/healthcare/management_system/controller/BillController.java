package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.ApiResponse;
import com.healthcare.management_system.dto.BillGenerationRequest;
import com.healthcare.management_system.dto.BillPaymentRequest;
import com.healthcare.management_system.entity.Bill;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.service.BillService;
import com.healthcare.management_system.service.AuditLogService;
import com.healthcare.management_system.service.PatientService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Billing", description = "Billing generation, retrieval, and payment APIs")
public class BillController {

    private final BillService billService;
    private final AuditLogService auditLogService;
    private final PatientService patientService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Bill>> generateBill(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BillGenerationRequest request) {
        Bill bill = billService.generateBill(user, request.getPatientId(), request.getAmount(), request.getDescription());
        return ResponseEntity.ok(ApiResponse.success("Bill generated successfully", bill));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<Bill>>> getAllBills(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("All bills retrieved", billService.getAllBills(user)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<Bill>>> getPatientBills(
            @AuthenticationPrincipal User user,
            @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success("Bills retrieved", billService.getPatientBills(user, patientId)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<Bill>>> getMyBills(@AuthenticationPrincipal User user) {
        List<Bill> bills = billService.getMyBills(user);
        auditLogService.log(user, patientService.getPatientEntity(user), "VIEW_BILLS", "BILL_LIST", null,
                "Patient viewed billing history");
        return ResponseEntity.ok(ApiResponse.success("Bills retrieved", bills));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Bill>> markAsPaid(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody BillPaymentRequest request) {
        Bill bill = billService.markAsPaid(user, id, request.getPaymentMethod());
        return ResponseEntity.ok(ApiResponse.success("Payment recorded", bill));
    }

    @PutMapping("/{id}/pay/self")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Bill>> payMyBill(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody BillPaymentRequest request) {
        Bill bill = billService.payMyBill(user, id, request.getPaymentMethod());
        auditLogService.log(user, patientService.getPatientEntity(user), "PAY_BILL", "BILL",
                String.valueOf(bill.getId()), "Patient paid bill via " + bill.getPaymentMethod());
        return ResponseEntity.ok(ApiResponse.success("Payment completed", bill));
    }
}
