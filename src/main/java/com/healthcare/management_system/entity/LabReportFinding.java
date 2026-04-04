package com.healthcare.management_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lab_report_findings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabReportFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_report_id", nullable = false)
    private LabReport labReport;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private String status;
}
