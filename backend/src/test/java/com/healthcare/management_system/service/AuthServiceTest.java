package com.healthcare.management_system.service;

import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.enums.HospitalStatus;
import com.healthcare.management_system.enums.Role;
import com.healthcare.management_system.repository.HospitalRepository;
import com.healthcare.management_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRegisterHospital_Success() {
        // Arrange
        String name = "Test Hospital";
        String address = "Test Address";
        String contact = "1234567890";
        String email = "admin@test.com";
        String password = "password";
        String gstNumber = "GST123";
        String ownerName = "Dr. Test";
        String hospitalType = "Clinic";
        Double latitude = 10.0;
        Double longitude = 20.0;

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        
        Hospital savedHospital = Hospital.builder()
                .id(1L)
                .name(name)
                .hospitalCode("HOSP-1234")
                .status(HospitalStatus.PENDING)
                .build();
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(savedHospital);

        // Act
        authService.registerHospital(name, address, contact, email, password, 
                                     gstNumber, ownerName, hospitalType, latitude, longitude);

        // Assert
        ArgumentCaptor<Hospital> hospitalCaptor = ArgumentCaptor.forClass(Hospital.class);
        verify(hospitalRepository).save(hospitalCaptor.capture());
        Hospital capturedHospital = hospitalCaptor.getValue();
        
        assertEquals(name, capturedHospital.getName());
        assertEquals(gstNumber, capturedHospital.getGstNumber());
        assertEquals(ownerName, capturedHospital.getOwnerName());
        assertEquals(hospitalType, capturedHospital.getHospitalType());
        assertEquals(latitude, capturedHospital.getLatitude());
        assertEquals(longitude, capturedHospital.getLongitude());
        assertEquals(HospitalStatus.PENDING, capturedHospital.getStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        
        assertEquals(email, capturedUser.getEmail());
        assertEquals("encodedPassword", capturedUser.getPassword());
        assertEquals(Role.ADMIN, capturedUser.getRole());
        assertEquals(ownerName, capturedUser.getFullName());
    }
}
