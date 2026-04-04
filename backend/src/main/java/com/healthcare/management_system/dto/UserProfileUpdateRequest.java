package com.healthcare.management_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    
    // Common fields
    @Size(max = 120, message = "Full name must be at most 120 characters")
    private String fullName;

    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Phone number must be 7 to 20 valid characters")
    private String phone;
    
    // Patient specific fields
    @Size(max = 10, message = "Blood group must be at most 10 characters")
    private String bloodGroup;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Emergency contact must be 7 to 20 valid characters")
    private String emergencyContact;
    
    // Doctor specific fields
    @Size(max = 120, message = "Specialization must be at most 120 characters")
    private String specialization;

    @Size(max = 50, message = "License number must be at most 50 characters")
    private String licenseNumber;

    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fee must be greater than zero")
    private Double consultationFee;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Available from must be in HH:mm format")
    private String availableFrom; // Time format "HH:mm"

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Available to must be in HH:mm format")
    private String availableTo;   // Time format "HH:mm"
}
