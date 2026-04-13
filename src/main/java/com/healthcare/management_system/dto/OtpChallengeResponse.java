package com.healthcare.management_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpChallengeResponse {
    private String email;
    private String demoOtp;
    private String expiresIn;
}
