package com.healthcare.management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDirectoryResponse {
    private Long id;
    private String name;
    private String hospitalCode;
    private String address;
}
