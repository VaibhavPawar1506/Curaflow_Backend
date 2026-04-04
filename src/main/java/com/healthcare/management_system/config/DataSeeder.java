package com.healthcare.management_system.config;

import com.healthcare.management_system.entity.*;
import com.healthcare.management_system.enums.HospitalStatus;
import com.healthcare.management_system.enums.Role;
import com.healthcare.management_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final LabRepository labRepository;
    private final LabTestOfferingRepository labTestOfferingRepository;
    private final LabReportRepository labReportRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        ensureHospitalDepartmentsAndDoctorAssignments();
        seedDemoHospitalNetwork();
        seedDemoLabsAndReports();

        if (userRepository.existsByEmail("superadmin@test.com")) return;

        // Create Super Admin (Platform Owner)
        User superAdmin = User.builder()
                .fullName("System Administrator")
                .email("superadmin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.SUPER_ADMIN)
                .build();
        userRepository.save(superAdmin);

        Hospital hospital = ensureHospital("HOSP001", "General Care Hospital", "123 Health Ave");

        // Create Admin
        User admin = User.builder()
                .fullName("System Admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .hospital(hospital)
                .build();
        userRepository.save(admin);

        // Create Doctors for different sections
        createDoctor(hospital, "Dr. Alice Hart", "alice@test.com", "Cardiology", "LIC001", 12, 100.0);
        createDoctor(hospital, "Dr. Bob Child", "bob@test.com", "Pediatrics", "LIC002", 8, 80.0);
        createDoctor(hospital, "Dr. Charlie Bone", "charlie@test.com", "Orthopedics", "LIC003", 15, 120.0);
        createDoctor(hospital, "Dr. David Mind", "david@test.com", "Neurology", "LIC004", 10, 150.0);
        createDoctor(hospital, "Dr. Eve Skin", "eve@test.com", "Dermatology", "LIC005", 5, 90.0);
        
        // Create Receptionist
        User receptionist = User.builder()
                .fullName("Receptionist Jane")
                .email("staff@test.com")
                .password(passwordEncoder.encode("staff123"))
                .role(Role.RECEPTIONIST)
                .hospital(hospital)
                .build();
        userRepository.save(receptionist);
    }

    private void seedDemoHospitalNetwork() {
        seedHospitalWithDoctors(
                "HOSP101",
                "City Heart Institute",
                "45 Riverfront Road",
                List.of(
                        new DoctorSeed("Dr. Meera Kapoor", "meera.kapoor@test.com", "Cardiology", "CHI-CARD-001", 14, 120.0),
                        new DoctorSeed("Dr. Arjun Sen", "arjun.sen@test.com", "Neurology", "CHI-NEUR-002", 11, 140.0),
                        new DoctorSeed("Dr. Tara Nair", "tara.nair@test.com", "Radiology", "CHI-RAD-003", 9, 95.0)
                )
        );

        seedHospitalWithDoctors(
                "HOSP102",
                "Sunrise Multi-Speciality Hospital",
                "18 Lake View Avenue",
                List.of(
                        new DoctorSeed("Dr. Neha Bedi", "neha.bedi@test.com", "Pediatrics", "SMH-PED-001", 8, 85.0),
                        new DoctorSeed("Dr. Kiran Das", "kiran.das@test.com", "Orthopedics", "SMH-ORTHO-002", 13, 130.0),
                        new DoctorSeed("Dr. Rahul Joshi", "rahul.joshi@test.com", "Dermatology", "SMH-DERM-003", 7, 90.0)
                )
        );

        seedHospitalWithDoctors(
                "HOSP103",
                "Green Valley Medical Center",
                "77 Hill Park Street",
                List.of(
                        new DoctorSeed("Dr. Sana Ali", "sana.ali@test.com", "Dental", "GVM-DENT-001", 10, 75.0),
                        new DoctorSeed("Dr. Vikram Rao", "vikram.rao@test.com", "ENT", "GVM-ENT-002", 12, 88.0),
                        new DoctorSeed("Dr. Isha Menon", "isha.menon@test.com", "Gastroenterology", "GVM-GASTRO-003", 15, 150.0)
                )
        );
    }

    private void seedHospitalWithDoctors(String hospitalCode, String hospitalName, String address, List<DoctorSeed> doctors) {
        Hospital hospital = ensureHospital(hospitalCode, hospitalName, address);
        for (DoctorSeed doctorSeed : doctors) {
            ensureDoctor(hospital, doctorSeed);
        }
    }

    private Hospital ensureHospital(String hospitalCode, String hospitalName, String address) {
        Hospital hospital = hospitalRepository.findByHospitalCode(hospitalCode)
                .orElseGet(() -> hospitalRepository.save(Hospital.builder()
                        .name(hospitalName)
                        .hospitalCode(hospitalCode)
                        .address(address)
                        .status(HospitalStatus.APPROVED)
                        .build()));

        seedDefaultDepartments(hospital);
        return hospital;
    }

    private void ensureDoctor(Hospital hospital, DoctorSeed doctorSeed) {
        if (userRepository.existsByEmail(doctorSeed.email())) {
            return;
        }
        createDoctor(
                hospital,
                doctorSeed.fullName(),
                doctorSeed.email(),
                doctorSeed.specialization(),
                doctorSeed.licenseNumber(),
                doctorSeed.experienceYears(),
                doctorSeed.consultationFee()
        );
    }

    private void createDoctor(Hospital hospital, String name, String email, String spec, String lic, int exp, double fee) {
        User user = User.builder()
                .fullName(name)
                .email(email)
                .password(passwordEncoder.encode("doctor123"))
                .role(Role.DOCTOR)
                .hospital(hospital)
                .build();
        userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .department(resolveDepartment(hospital, spec))
                .specialization(spec)
                .licenseNumber(lic)
                .experienceYears(exp)
                .consultationFee(BigDecimal.valueOf(fee))
                .availableFrom(LocalTime.of(9, 0))
                .availableTo(LocalTime.of(17, 0))
                .build();
        doctorRepository.save(doctor);
    }

    private void ensureHospitalDepartmentsAndDoctorAssignments() {
        for (Hospital hospital : hospitalRepository.findAll()) {
            seedDefaultDepartments(hospital);
            for (Doctor doctor : doctorRepository.findByUser_Hospital(hospital)) {
                if (doctor.getDepartment() == null) {
                    doctor.setDepartment(resolveDepartment(hospital, doctor.getSpecialization()));
                    doctorRepository.save(doctor);
                }
            }
        }
    }

    private void seedDefaultDepartments(Hospital hospital) {
        for (String departmentName : new String[] {
                "Cardiology", "Oncology", "Eyecare", "Pediatrics", "Neurology",
                "Orthopedics", "Radiology", "Dental", "ENT", "Gastroenterology", "Dermatology"
        }) {
            departmentRepository.findByHospitalIdAndNameIgnoreCase(hospital.getId(), departmentName)
                    .orElseGet(() -> departmentRepository.save(Department.builder()
                            .name(departmentName)
                            .description("Default " + departmentName + " department.")
                            .hospital(hospital)
                            .active(true)
                            .build()));
        }
    }

    private Department resolveDepartment(Hospital hospital, String specialization) {
        String departmentName = specialization == null || specialization.isBlank() ? "General Medicine" : specialization;
        return departmentRepository.findByHospitalIdAndNameIgnoreCase(hospital.getId(), departmentName)
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .name(departmentName)
                        .description("Department for " + departmentName + ".")
                        .hospital(hospital)
                        .active(true)
                        .build()));
    }

    private void seedDemoLabsAndReports() {
        Lab labOne = ensureLab(
                "LAB001",
                "Cura Diagnostics Center",
                "12 MG Road, Bengaluru",
                "080-1000-1000",
                "7:00 AM - 9:00 PM",
                true,
                List.of(
                        new LabTestSeed("Complete Blood Count", "Pathology", "Blood", false, 450),
                        new LabTestSeed("Thyroid Profile", "Hormones", "Blood", false, 650),
                        new LabTestSeed("Vitamin D", "Wellness", "Blood", false, 900)
                )
        );

        Lab labTwo = ensureLab(
                "LAB002",
                "HealthFirst Pathology Lab",
                "Anna Nagar, Chennai",
                "044-2200-2200",
                "8:00 AM - 8:00 PM",
                false,
                List.of(
                        new LabTestSeed("HbA1c", "Diabetes", "Blood", false, 500),
                        new LabTestSeed("Liver Function Test", "Pathology", "Blood", true, 700),
                        new LabTestSeed("Urine Routine", "Pathology", "Urine", false, 250)
                )
        );

        for (Patient patient : patientRepository.findAll()) {
            seedLabReportForPatientIfMissing(patient, labOne, "Complete Blood Count",
                    "Blood", false, "Normal",
                    "Hemoglobin, WBC, and platelet counts are within the normal reference range.",
                    List.of(
                            new LabFindingSeed("Hemoglobin", "13.4 g/dL", "Normal"),
                            new LabFindingSeed("WBC", "7,600 /uL", "Normal"),
                            new LabFindingSeed("Platelets", "2.8 lakh/uL", "Normal")
                    ));

            seedLabReportForPatientIfMissing(patient, labTwo, "Thyroid Profile",
                    "Blood", false, "Needs Review",
                    "TSH is mildly elevated. Clinical review is advised along with follow-up testing.",
                    List.of(
                            new LabFindingSeed("TSH", "5.9 uIU/mL", "High"),
                            new LabFindingSeed("T3", "1.21 ng/mL", "Normal"),
                            new LabFindingSeed("T4", "8.4 ug/dL", "Normal")
                    ));
        }
    }

    private Lab ensureLab(String code, String name, String address, String contact, String openHours, boolean homeCollection, List<LabTestSeed> tests) {
        Lab lab = labRepository.findByLabCode(code)
                .orElseGet(() -> labRepository.save(Lab.builder()
                        .name(name)
                        .labCode(code)
                        .address(address)
                        .contactNumber(contact)
                        .openHours(openHours)
                        .homeCollectionAvailable(homeCollection)
                        .active(true)
                        .build()));

        for (LabTestSeed test : tests) {
            labTestOfferingRepository.findByLabAndTestNameIgnoreCase(lab, test.testName())
                    .orElseGet(() -> labTestOfferingRepository.save(LabTestOffering.builder()
                            .lab(lab)
                            .testName(test.testName())
                            .category(test.category())
                            .sampleType(test.sampleType())
                            .fastingRequired(test.fastingRequired())
                            .price(BigDecimal.valueOf(test.price()))
                            .build()));
        }

        return lab;
    }

    private void seedLabReportForPatientIfMissing(Patient patient, Lab lab, String testName, String sampleType, boolean fastingRequired,
                                                  String status, String summary, List<LabFindingSeed> findings) {
        boolean exists = labReportRepository.findByPatientOrderByReportDateDesc(patient)
                .stream()
                .anyMatch((report) -> report.getLab().getId().equals(lab.getId()) && report.getTestName().equalsIgnoreCase(testName));

        if (exists) {
            return;
        }

        LabReport report = LabReport.builder()
                .patient(patient)
                .lab(lab)
                .testName(testName)
                .sampleType(sampleType)
                .fastingRequired(fastingRequired)
                .status(status)
                .summary(summary)
                .build();

        List<LabReportFinding> findingEntities = findings.stream()
                .map((finding) -> LabReportFinding.builder()
                        .labReport(report)
                        .label(finding.label())
                        .value(finding.value())
                        .status(finding.status())
                        .build())
                .toList();

        report.setFindings(findingEntities);
        labReportRepository.save(report);
    }

    private record DoctorSeed(
            String fullName,
            String email,
            String specialization,
            String licenseNumber,
            int experienceYears,
            double consultationFee
    ) {}

    private record LabTestSeed(
            String testName,
            String category,
            String sampleType,
            boolean fastingRequired,
            double price
    ) {}

    private record LabFindingSeed(
            String label,
            String value,
            String status
    ) {}
}
