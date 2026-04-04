package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.AuthResponse;
import com.healthcare.management_system.dto.MobilePatientRegistrationRequest;
import com.healthcare.management_system.dto.OtpChallengeResponse;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.Patient;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.enums.HospitalStatus;
import com.healthcare.management_system.enums.Role;
import com.healthcare.management_system.exception.BadRequestException;
import com.healthcare.management_system.repository.HospitalRepository;
import com.healthcare.management_system.repository.PatientRepository;
import com.healthcare.management_system.repository.UserRepository;
import com.healthcare.management_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MobileOtpService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.mobile.default-hospital-code:HOSP001}")
    private String defaultHospitalCode;

    private final Map<String, PendingMobileRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    public OtpChallengeResponse requestPatientRegistrationOtp(MobilePatientRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Account already exists. Proceed to sign in.");
        }

        Hospital hospital = resolveApprovedHospital();
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        pendingRegistrations.put(
                request.getEmail().trim().toLowerCase(),
                new PendingMobileRegistration(
                        request.getUsername().trim(),
                        request.getEmail().trim().toLowerCase(),
                        request.getMobileNumber().trim(),
                        request.getPassword(),
                        otp,
                        LocalDateTime.now().plusMinutes(5),
                        hospital.getId()
                )
        );

        return OtpChallengeResponse.builder()
                .email(request.getEmail().trim().toLowerCase())
                .demoOtp(otp)
                .expiresIn("5 minutes")
                .build();
    }

    public AuthResponse verifyPatientRegistrationOtp(String email, String otp) {
        String normalizedEmail = email.trim().toLowerCase();
        PendingMobileRegistration pendingRegistration = pendingRegistrations.get(normalizedEmail);

        if (pendingRegistration == null) {
            throw new BadRequestException("No OTP challenge found. Request a new OTP.");
        }

        if (pendingRegistration.expiresAt().isBefore(LocalDateTime.now())) {
            pendingRegistrations.remove(normalizedEmail);
            throw new BadRequestException("OTP expired. Request a new OTP.");
        }

        if (!pendingRegistration.otp().equals(otp)) {
            throw new BadRequestException("Invalid OTP");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            pendingRegistrations.remove(normalizedEmail);
            throw new BadRequestException("Account already exists. Proceed to sign in.");
        }

        Hospital hospital = hospitalRepository.findById(pendingRegistration.hospitalId())
                .orElseThrow(() -> new BadRequestException("Configured hospital not found"));

        User user = User.builder()
                .fullName(pendingRegistration.username())
                .email(pendingRegistration.email())
                .password(passwordEncoder.encode(pendingRegistration.password()))
                .phone(pendingRegistration.mobileNumber())
                .role(Role.PATIENT)
                .hospital(hospital)
                .build();

        user = userRepository.save(user);
        patientRepository.save(Patient.builder().user(user).build());
        pendingRegistrations.remove(normalizedEmail);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .hospitalName(hospital.getName())
                .hospitalCode(hospital.getHospitalCode())
                .hospitalId(hospital.getId())
                .build();
    }

    private Hospital resolveApprovedHospital() {
        Hospital hospital = hospitalRepository.findByHospitalCode(defaultHospitalCode)
                .orElseThrow(() -> new BadRequestException("Default patient hospital code is not configured correctly"));

        if (hospital.getStatus() != HospitalStatus.APPROVED) {
            throw new BadRequestException("Default patient hospital is not approved yet");
        }

        return hospital;
    }

    private record PendingMobileRegistration(
            String username,
            String email,
            String mobileNumber,
            String password,
            String otp,
            LocalDateTime expiresAt,
            Long hospitalId
    ) {}
}
