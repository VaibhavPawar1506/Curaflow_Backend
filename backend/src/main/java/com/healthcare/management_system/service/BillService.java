package com.healthcare.management_system.service;

import com.healthcare.management_system.entity.Bill;
import com.healthcare.management_system.entity.Patient;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.exception.BadRequestException;
import com.healthcare.management_system.exception.ResourceNotFoundException;
import com.healthcare.management_system.repository.BillRepository;
import com.healthcare.management_system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final PatientRepository patientRepository;

    public Bill generateBill(User user, Long patientId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Bill amount must be greater than zero");
        }

        Patient patient = patientRepository.findByUser_HospitalAndId(user.getHospital(), patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Bill bill = Bill.builder()
                .patient(patient)
                .amount(amount)
                .description(description)
                .status("PENDING")
                .build();

        return billRepository.save(bill);
    }

    public List<Bill> getPatientBills(User user, Long patientId) {
        Patient patient = patientRepository.findByUser_HospitalAndId(user.getHospital(), patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return billRepository.findByPatientOrderByCreatedAtDesc(patient);
    }

    public List<Bill> getMyBills(User user) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return billRepository.findByPatientOrderByCreatedAtDesc(patient);
    }

    public List<Bill> getAllBills(User user) {
        return billRepository.findByPatient_User_HospitalOrderByCreatedAtDesc(user.getHospital());
    }

    public Bill markAsPaid(User user, Long billId, String paymentMethod) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getPatient().getUser().getHospital() == null
                || user.getHospital() == null
                || !bill.getPatient().getUser().getHospital().getId().equals(user.getHospital().getId())) {
            throw new ResourceNotFoundException("Bill not found");
        }

        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new BadRequestException("Payment method is required");
        }
        
        bill.setStatus("PAID");
        bill.setPaymentMethod(paymentMethod);
        bill.setPaidAt(java.time.LocalDateTime.now());
        
        return billRepository.save(bill);
    }

    public Bill payMyBill(User user, Long billId, String paymentMethod) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (!bill.getPatient().getId().equals(patient.getId())) {
            throw new ResourceNotFoundException("Bill not found");
        }

        if (!"PENDING".equalsIgnoreCase(bill.getStatus())) {
            throw new BadRequestException("Only pending bills can be paid");
        }

        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new BadRequestException("Payment method is required");
        }

        bill.setStatus("PAID");
        bill.setPaymentMethod(paymentMethod);
        bill.setPaidAt(java.time.LocalDateTime.now());

        return billRepository.save(bill);
    }
}
