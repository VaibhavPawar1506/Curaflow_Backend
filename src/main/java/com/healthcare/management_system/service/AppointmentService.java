package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.AppointmentRequest;
import com.healthcare.management_system.dto.AppointmentResponse;
import com.healthcare.management_system.entity.Appointment;
import com.healthcare.management_system.entity.Doctor;
import com.healthcare.management_system.entity.Patient;
import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.enums.AppointmentStatus;
import com.healthcare.management_system.enums.Role;
import com.healthcare.management_system.exception.BadRequestException;
import com.healthcare.management_system.exception.ResourceNotFoundException;
import com.healthcare.management_system.repository.AppointmentRepository;
import com.healthcare.management_system.repository.DoctorRepository;
import com.healthcare.management_system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public AppointmentResponse bookAppointment(User user, AppointmentRequest request) {
        // Only patients can book appointments
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Only patients can book appointments"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        // Validate appointment is in the future
        if (request.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment date must be in the future");
        }

        // Check for scheduling conflicts (same doctor, within 30 min window)
        LocalDateTime start = request.getAppointmentDateTime().minusMinutes(30);
        LocalDateTime end = request.getAppointmentDateTime().plusMinutes(30);
        List<Appointment> conflicts = appointmentRepository
                .findByDoctorAndAppointmentDateTimeBetween(doctor, start, end);

        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Doctor is not available at the requested time");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(request.getAppointmentDateTime())
                .status(AppointmentStatus.SCHEDULED)
                .reason(request.getReason())
                .build();

        appointment = appointmentRepository.save(appointment);
        return mapToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, User user) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        // Verify the user owns this appointment (patient or doctor)
        boolean isPatient = appointment.getPatient().getUser().getId().equals(user.getId());
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(user.getId());
        boolean isHospitalOperator = (user.getRole() == Role.ADMIN || user.getRole() == Role.RECEPTIONIST)
                && user.getHospital() != null
                && appointment.getDoctor().getUser().getHospital() != null
                && appointment.getDoctor().getUser().getHospital().getId().equals(user.getHospital().getId());

        if (!isPatient && !isDoctor && !isHospitalOperator) {
            throw new BadRequestException("You are not authorized to cancel this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);
        return mapToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long appointmentId, User user) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        // Only the assigned doctor can complete an appointment
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(user.getId());
        if (!isDoctor) {
            throw new BadRequestException("Only the assigned doctor can complete this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled appointments can be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment = appointmentRepository.save(appointment);
        return mapToResponse(appointment);
    }

    public List<AppointmentResponse> getMyAppointments(User user) {
        List<Appointment> appointments;

        if (user.getRole() == Role.PATIENT) {
            Patient patient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
            appointments = appointmentRepository.findByPatientOrderByAppointmentDateTimeDesc(patient);
        } else if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
            appointments = appointmentRepository.findByDoctorOrderByAppointmentDateTimeDesc(doctor);
        } else {
            appointments = appointmentRepository.findByDoctor_User_HospitalOrderByAppointmentDateTimeDesc(user.getHospital());
        }

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public org.springframework.data.domain.Page<AppointmentResponse> filterMyAppointments(
            User user,
            AppointmentStatus status,
            String startDateStr,
            String endDateStr,
            int page,
            int size) {
        
        Long patientId = null;
        Long doctorId = null;
        Long hospitalId = user.getHospital() != null ? user.getHospital().getId() : null;

        if (user.getRole() == Role.PATIENT) {
            Patient patient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
            patientId = patient.getId();
        } else if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
            doctorId = doctor.getId();
        }
        
        LocalDateTime startDate = startDateStr != null ? LocalDateTime.parse(startDateStr) : null;
        LocalDateTime endDate = endDateStr != null ? LocalDateTime.parse(endDateStr) : null;

        return appointmentRepository.filterAppointments(
                hospitalId, doctorId, patientId, status, startDate, endDate, org.springframework.data.domain.PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getUser().getFullName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getUser().getFullName())
                .hospitalId(appointment.getDoctor().getUser().getHospital() != null ? appointment.getDoctor().getUser().getHospital().getId() : null)
                .hospitalName(appointment.getDoctor().getUser().getHospital() != null ? appointment.getDoctor().getUser().getHospital().getName() : null)
                .departmentId(appointment.getDoctor().getDepartment() != null ? appointment.getDoctor().getDepartment().getId() : null)
                .departmentName(appointment.getDoctor().getDepartment() != null ? appointment.getDoctor().getDepartment().getName() : null)
                .specialization(appointment.getDoctor().getSpecialization())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
