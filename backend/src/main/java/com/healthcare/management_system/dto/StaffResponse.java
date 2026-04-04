package com.healthcare.management_system.dto;

import com.healthcare.management_system.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String specialization;
    private boolean active;
}
