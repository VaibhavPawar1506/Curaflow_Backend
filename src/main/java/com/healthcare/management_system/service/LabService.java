package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.*;
import com.healthcare.management_system.entity.*;
import com.healthcare.management_system.enums.LabAppointmentStatus;
import com.healthcare.management_system.exception.BadRequestException;
import com.healthcare.management_system.exception.ResourceNotFoundException;
import com.healthcare.management_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabRepository labRepository;
    private final LabTestOfferingRepository labTestOfferingRepository;
    private final LabAppointmentRepository labAppointmentRepository;
    private final LabReportRepository labReportRepository;
    private final PatientRepository patientRepository;

    public List<LabDirectoryResponse> getActiveLabs() {
        return labRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::mapLabDirectory)
                .collect(Collectors.toList());
    }

    public LabAppointmentResponse bookAppointment(User user, LabAppointmentCreateRequest request) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        Lab lab = labRepository.findById(request.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Lab not found with id: " + request.getLabId()));

        if (!Boolean.TRUE.equals(lab.getActive())) {
            throw new BadRequestException("This lab is not available for booking");
        }

        validateSelectedTests(lab, request.getSelectedTests());

        LabAppointment appointment = labAppointmentRepository.save(LabAppointment.builder()
                .patient(patient)
                .lab(lab)
                .selectedTests(request.getSelectedTests())
                .appointmentDateTime(request.getAppointmentDateTime())
                .status(LabAppointmentStatus.SCHEDULED)
                .homeCollection(Boolean.TRUE.equals(request.getHomeCollection()))
                .notes(request.getNotes())
                .build());

        return mapAppointment(appointment);
    }

    public List<LabAppointmentResponse> getMyAppointments(User user) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return labAppointmentRepository.findByPatientOrderByAppointmentDateTimeDesc(patient)
                .stream()
                .map(this::mapAppointment)
                .collect(Collectors.toList());
    }

    public List<LabReportResponse> getMyReports(User user) {
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return labReportRepository.findByPatientOrderByReportDateDesc(patient)
                .stream()
                .map(this::mapReport)
                .collect(Collectors.toList());
    }

    private void validateSelectedTests(Lab lab, List<String> selectedTests) {
        List<String> offeredTests = labTestOfferingRepository.findByLabOrderByCategoryAscTestNameAsc(lab)
                .stream()
                .map(LabTestOffering::getTestName)
                .collect(Collectors.toList());

        for (String testName : selectedTests) {
            if (offeredTests.stream().noneMatch((offered) -> offered.equalsIgnoreCase(testName))) {
                throw new BadRequestException("Selected test is not offered by the chosen lab: " + testName);
            }
        }
    }

    private LabDirectoryResponse mapLabDirectory(Lab lab) {
        return LabDirectoryResponse.builder()
                .id(lab.getId())
                .name(lab.getName())
                .labCode(lab.getLabCode())
                .address(lab.getAddress())
                .contactNumber(lab.getContactNumber())
                .openHours(lab.getOpenHours())
                .homeCollectionAvailable(lab.getHomeCollectionAvailable())
                .tests(labTestOfferingRepository.findByLabOrderByCategoryAscTestNameAsc(lab)
                        .stream()
                        .map(this::mapOffering)
                        .collect(Collectors.toList()))
                .build();
    }

    private LabTestOfferingResponse mapOffering(LabTestOffering offering) {
        return LabTestOfferingResponse.builder()
                .id(offering.getId())
                .testName(offering.getTestName())
                .category(offering.getCategory())
                .sampleType(offering.getSampleType())
                .fastingRequired(offering.getFastingRequired())
                .price(offering.getPrice())
                .build();
    }

    private LabAppointmentResponse mapAppointment(LabAppointment appointment) {
        return LabAppointmentResponse.builder()
                .id(appointment.getId())
                .labId(appointment.getLab().getId())
                .labName(appointment.getLab().getName())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .status(appointment.getStatus())
                .homeCollection(appointment.getHomeCollection())
                .notes(appointment.getNotes())
                .selectedTests(appointment.getSelectedTests())
                .createdAt(appointment.getCreatedAt())
                .build();
    }

    private LabReportResponse mapReport(LabReport report) {
        return LabReportResponse.builder()
                .id(report.getId())
                .labId(report.getLab().getId())
                .labName(report.getLab().getName())
                .testName(report.getTestName())
                .sampleType(report.getSampleType())
                .fastingRequired(report.getFastingRequired())
                .status(report.getStatus())
                .summary(report.getSummary())
                .reportDate(report.getReportDate())
                .findings(report.getFindings()
                        .stream()
                        .map((finding) -> LabReportFindingResponse.builder()
                                .label(finding.getLabel())
                                .value(finding.getValue())
                                .status(finding.getStatus())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
