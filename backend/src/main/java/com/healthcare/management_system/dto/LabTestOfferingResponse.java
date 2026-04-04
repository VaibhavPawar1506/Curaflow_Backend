package com.healthcare.management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestOfferingResponse {
    private Long id;
    private String testName;
    private String category;
    private String sampleType;
    private Boolean fastingRequired;
    private BigDecimal price;
}
