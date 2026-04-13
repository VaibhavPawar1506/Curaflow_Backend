package com.healthcare.management_system.entity;

import com.healthcare.management_system.enums.LabAppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lab_appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @ElementCollection
    @CollectionTable(name = "lab_appointment_tests", joinColumns = @JoinColumn(name = "lab_appointment_id"))
    @Column(name = "test_name", nullable = false)
    @Builder.Default
    private List<String> selectedTests = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime appointmentDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LabAppointmentStatus status;

    @Column(nullable = false)
    private Boolean homeCollection;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
