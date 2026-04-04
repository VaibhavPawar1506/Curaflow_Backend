package com.healthcare.management_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lab_test_offerings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @Column(nullable = false)
    private String testName;

    private String category;

    private String sampleType;

    @Column(nullable = false)
    private Boolean fastingRequired;

    private BigDecimal price;
}
