package com.healthcare.management_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bill_id", nullable = false, unique = true)
    private Bill bill;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(nullable = false)
    private String providerName;

    @Column(nullable = false)
    private String policyNumber;

    @Column(nullable = false)
    private BigDecimal claimedAmount;

    private BigDecimal coveredAmount;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, PARTIALLY_PAID

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }
}
