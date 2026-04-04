package com.healthcare.management_system.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterHospitalRequest {
    @NotBlank(message = "Hospital name is required")
    @Size(max = 120, message = "Hospital name must be at most 120 characters")
    private String name;

    @NotBlank(message = "Hospital address is required")
    @Size(max = 500, message = "Hospital address must be at most 500 characters")
    private String address;

    @NotBlank(message = "Contact number is required")
    @Size(min = 7, max = 20, message = "Contact number must be between 7 and 20 characters")
    private String contact;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Size(max = 50, message = "GST number must be at most 50 characters")
    private String gstNumber;

    @NotBlank(message = "Owner name is required")
    @Size(max = 120, message = "Owner name must be at most 120 characters")
    private String ownerName;

    @NotBlank(message = "Hospital type is required")
    @Size(max = 50, message = "Hospital type must be at most 50 characters")
    private String hospitalType;

    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
    @DecimalMax(value = "90.0", message = "Latitude must be at most 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
    @DecimalMax(value = "180.0", message = "Longitude must be at most 180")
    private Double longitude;
}
