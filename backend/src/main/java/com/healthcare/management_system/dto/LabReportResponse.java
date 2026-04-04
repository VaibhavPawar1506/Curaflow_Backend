package com.healthcare.management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabReportResponse {
    private Long id;
    private Long labId;
    private String labName;
    private String testName;
    private String sampleType;
    private Boolean fastingRequired;
    private String status;
    private String summary;
    private LocalDate reportDate;
    private List<LabReportFindingResponse> findings;
}
