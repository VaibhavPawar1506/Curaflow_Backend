package com.healthcare.management_system.entity;

import com.healthcare.management_system.enums.HospitalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hospitals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String hospitalCode;

    private String address;

    private String contactNumber;

    private String gstNumber;

    private String ownerName;

    private String hospitalType;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HospitalStatus status;

    private String verificationNotes;
    private String verificationOfficer;
    private LocalDateTime inspectionScheduledAt;
    private LocalDateTime physicallyVerifiedAt;
    private Boolean locationVerified;

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Department> departments;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
