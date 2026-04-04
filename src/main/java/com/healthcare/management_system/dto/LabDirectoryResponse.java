package com.healthcare.management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabDirectoryResponse {
    private Long id;
    private String name;
    private String labCode;
    private String address;
    private String contactNumber;
    private String openHours;
    private Boolean homeCollectionAvailable;
    private List<LabTestOfferingResponse> tests;
}
