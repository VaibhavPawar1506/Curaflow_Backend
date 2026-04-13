package com.healthcare.management_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "medical_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = true)
    private Doctor doctor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String prescription;

    private String prescriptionFile;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Double weight;
    private Double height;
    private String bloodGroup;
    private String bloodPressure;
    private Integer heartbeat;
    private Integer bloodOxygen;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(updatable = false)
    private LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        if (recordDate == null) {
            recordDate = LocalDate.now();
        }
    }
}
