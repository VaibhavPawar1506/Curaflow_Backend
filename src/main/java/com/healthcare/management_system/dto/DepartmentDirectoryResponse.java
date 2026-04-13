package com.healthcare.management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDirectoryResponse {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private Long hospitalId;
}
