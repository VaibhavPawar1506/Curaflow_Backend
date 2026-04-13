package com.healthcare.management_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InsuranceClaimRequest {
    @NotNull(message = "Bill ID is required")
    private Long billId;

    @NotNull(message = "Claimed amount is required")
    private BigDecimal claimedAmount;
}
