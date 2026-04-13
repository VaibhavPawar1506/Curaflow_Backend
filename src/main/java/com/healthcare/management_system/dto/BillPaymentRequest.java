package com.healthcare.management_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BillPaymentRequest {

    @NotBlank(message = "Payment method is required")
    @jakarta.validation.constraints.Size(max = 50, message = "Payment method must be at most 50 characters")
    private String paymentMethod;
}
