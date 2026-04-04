package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.healthcare.management_system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Public authentication and hospital onboarding endpoints")
public class AuthController {

    private final AuthService authService;
    private final com.healthcare.management_system.service.MobileOtpService mobileOtpService;

    @PostMapping("/register")
    @Operation(summary = "Register a patient or doctor", description = "Creates a hospital-linked user account using a valid hospital code.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in", description = "Authenticates a user and returns a JWT bearer token for secured APIs.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/oauth/google")
    @Operation(summary = "Google Login", description = "Authenticates a user via Google ID token.")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody SocialLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request.getToken(), request.getRole());
        return ResponseEntity.ok(ApiResponse.success("Google login successful", response));
    }

    @PostMapping("/oauth/apple")
    @Operation(summary = "Apple Login", description = "Authenticates a user via Apple identity token.")
    public ResponseEntity<ApiResponse<AuthResponse>> appleLogin(@Valid @RequestBody SocialLoginRequest request) {
        AuthResponse response = authService.loginWithApple(request.getToken(), request.getRole());
        return ResponseEntity.ok(ApiResponse.success("Apple login successful", response));
    }

    @PostMapping("/oauth/facebook")
    @Operation(summary = "Facebook Login", description = "Authenticates a user via Facebook access token.")
    public ResponseEntity<ApiResponse<AuthResponse>> facebookLogin(@Valid @RequestBody SocialLoginRequest request) {
        AuthResponse response = authService.loginWithFacebook(request.getToken(), request.getRole());
        return ResponseEntity.ok(ApiResponse.success("Facebook login successful", response));
    }

    @PostMapping("/mobile/patient/register/request-otp")
    @Operation(summary = "Request patient registration OTP", description = "Creates a demo OTP challenge for mobile patient registration.")
    public ResponseEntity<ApiResponse<OtpChallengeResponse>> requestMobilePatientOtp(
            @Valid @RequestBody MobilePatientRegistrationRequest request) {
        OtpChallengeResponse response = mobileOtpService.requestPatientRegistrationOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP generated for demo verification", response));
    }

    @PostMapping("/mobile/patient/register/verify-otp")
    @Operation(summary = "Verify patient registration OTP", description = "Verifies the OTP and creates the patient account for the mobile app.")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyMobilePatientOtp(
            @Valid @RequestBody OtpVerificationRequest request) {
        AuthResponse response = mobileOtpService.verifyPatientRegistrationOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient registration completed", response));
    }

    @PostMapping("/hospital/register")
    @Operation(summary = "Register a hospital", description = "Creates a pending hospital registration that must pass physical verification and super admin approval.")
    public ResponseEntity<ApiResponse<String>> registerHospital(@Valid @RequestBody RegisterHospitalRequest request) {
        authService.registerHospital(
                request.getName(), 
                request.getAddress(), 
                request.getContact(), 
                request.getEmail(), 
                request.getPassword(),
                request.getGstNumber(),
                request.getOwnerName(),
                request.getHospitalType(),
                request.getLatitude(),
                request.getLongitude()
        );
        return ResponseEntity.ok(ApiResponse.success("Hospital registration submitted for verification", null));
    }
}
