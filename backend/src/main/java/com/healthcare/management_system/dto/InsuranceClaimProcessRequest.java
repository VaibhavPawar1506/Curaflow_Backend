package com.healthcare.management_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InsuranceClaimProcessRequest {
    @NotNull(message = "Status is required (APPROVED, REJECTED, PARTIALLY_PAID)")
    private String status;

    private BigDecimal coveredAmount;
    private String remarks;
}
