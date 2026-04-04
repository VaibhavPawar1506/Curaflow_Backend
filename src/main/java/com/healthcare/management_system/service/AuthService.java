package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.AuthResponse;
import com.healthcare.management_system.dto.LoginRequest;
import com.healthcare.management_system.dto.RegisterRequest;
import com.healthcare.management_system.entity.Doctor;
import com.healthcare.management_system.entity.Patient;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.enums.Role;
import com.healthcare.management_system.exception.BadRequestException;
import com.healthcare.management_system.repository.DoctorRepository;
import com.healthcare.management_system.repository.PatientRepository;
import com.healthcare.management_system.repository.HospitalRepository;
import com.healthcare.management_system.repository.UserRepository;
import com.healthcare.management_system.enums.HospitalStatus;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        validateRegistrationRequest(request);

        var hospital = hospitalRepository.findByHospitalCode(request.getHospitalCode())
                .orElseThrow(() -> new BadRequestException("Invalid hospital code"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .hospital(hospital)
                .build();

        user = userRepository.save(user);

        if (request.getRole() == Role.PATIENT) {
            Patient patient = Patient.builder()
                    .user(user)
                    .build();
            patientRepository.save(patient);
        } else if (request.getRole() == Role.DOCTOR) {
            Doctor doctor = Doctor.builder()
                    .user(user)
                    .specialization(request.getSpecialization())
                    .licenseNumber(request.getLicenseNumber())
                    .consultationFee(java.math.BigDecimal.valueOf(50.0))
                    .build();
            doctorRepository.save(doctor);
        }

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

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Bypass hospital checks for SUPER_ADMIN
        if (user.getRole() != Role.SUPER_ADMIN) {
            if (user.getHospital() == null) {
                throw new BadRequestException("User is not associated with any hospital. Please contact administrator.");
            }

            if (request.getHospitalCode() != null
                    && !request.getHospitalCode().isBlank()
                    && !user.getHospital().getHospitalCode().equalsIgnoreCase(request.getHospitalCode())) {
                throw new BadRequestException("Invalid hospital code for this user");
            }

            if (user.getHospital().getStatus() != HospitalStatus.APPROVED) {
                throw new BadRequestException("Your hospital account is not active yet. Physical site verification and final approval must be completed before login.");
            }
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .hospitalName(user.getHospital() != null ? user.getHospital().getName() : "System Platform")
                .hospitalCode(user.getHospital() != null ? user.getHospital().getHospitalCode() : "SYSTEM")
                .hospitalId(user.getHospital() != null ? user.getHospital().getId() : null)
                .build();
    }

    @Transactional
    public void registerHospital(String name, String address, String contact, String email, String password, 
                                 String gstNumber, String ownerName, String hospitalType, 
                                 Double latitude, Double longitude) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Admin email already registered");
        }

        Hospital hospital = Hospital.builder()
                .name(name)
                .address(address)
                .contactNumber(contact)
                .gstNumber(gstNumber)
                .ownerName(ownerName)
                .hospitalType(hospitalType)
                .latitude(latitude)
                .longitude(longitude)
                .hospitalCode("HOSP-" + System.currentTimeMillis() % 10000)
                .status(HospitalStatus.PENDING)
                .build();
        hospital = hospitalRepository.save(hospital);

        User admin = User.builder()
                .fullName(ownerName) // Using owner name for initial admin
                .email(email)
                .password(passwordEncoder.encode(password))
                .phone(contact)
                .role(Role.ADMIN)
                .hospital(hospital)
                .build();
            userRepository.save(admin);
    }

    @Transactional
    public AuthResponse loginWithGoogle(String token, Role role) {
        // Mock verification - in production, verify with Google APIs
        String email = "google_user@example.com";
        return processSocialLogin(email, "Google User", role);
    }

    @Transactional
    public AuthResponse loginWithApple(String token, Role role) {
        // Mock verification - in production, verify with Apple APIs
        String email = "apple_user@example.com";
        return processSocialLogin(email, "Apple User", role);
    }

    @Transactional
    public AuthResponse loginWithFacebook(String token, Role role) {
        // Mock verification - in production, verify with Facebook APIs
        String email = "facebook_user@example.com";
        return processSocialLogin(email, "Facebook User", role);
    }

    private AuthResponse processSocialLogin(String email, String fullName, Role role) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Create new user if not exists
                    User newUser = User.builder()
                            .email(email)
                            .fullName(fullName)
                            .password(passwordEncoder.encode("OAUTH_USER_" + System.currentTimeMillis()))
                            .role(role != null ? role : Role.PATIENT)
                            .build();
                    return userRepository.save(newUser);
                });

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .hospitalName(user.getHospital() != null ? user.getHospital().getName() : "System Platform")
                .hospitalCode(user.getHospital() != null ? user.getHospital().getHospitalCode() : "SYSTEM")
                .hospitalId(user.getHospital() != null ? user.getHospital().getId() : null)
                .build();
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (request.getRole() == Role.DOCTOR) {
            if (request.getSpecialization() == null || request.getSpecialization().isBlank()) {
                throw new BadRequestException("Specialization is required for doctor registration");
            }
            if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
                throw new BadRequestException("License number is required for doctor registration");
            }
        }
    }
}
