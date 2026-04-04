package com.healthcare.management_system.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long patientId;
    private String patientName;
    private String action;
    private String targetType;
    private String targetId;
    private String details;
    private LocalDateTime createdAt;
}
