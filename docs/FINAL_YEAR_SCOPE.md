# Final Year Scope Freeze

This document freezes the academic project scope for the final-year version of the Universal EHR system.

## In Scope

- Spring Boot backend with JWT authentication
- React web frontend for:
  - Super Admin
  - Hospital Admin
  - Doctor
  - Receptionist
  - Patient
- React Native patient mobile app
- Hospital registration with supervisor-led physical verification
- Patient, doctor, appointment, billing, department, and medical-record modules
- Hospital-scoped access control
- Seeded demo data and reset tooling for presentations

## Out of Scope

- National-scale deployment
- Full legal/compliance certification
- Full HL7/FHIR interoperability implementation
- Production-grade audit/compliance platform
- Real geo-verification hardware integration

## Frozen Core Entities

- `User`
- `Hospital`
- `Patient`
- `Doctor`
- `Appointment`
- `MedicalRecord`
- `Bill`
- `Department`

## Frozen Role Model

- `PATIENT`
- `DOCTOR`
- `ADMIN`
- `RECEPTIONIST`
- `SUPER_ADMIN`

## Frozen Hospital Status Flow

1. `PENDING`
2. `INSPECTION_SCHEDULED`
3. `PHYSICALLY_VERIFIED`
4. `APPROVED`
5. `REJECTED`

## Frozen Core API Areas

- `/api/auth`
- `/api/superadmin`
- `/api/admin`
- `/api/patients`
- `/api/doctors`
- `/api/appointments`
- `/api/medical-records`
- `/api/bills`
- `/api/departments`
- `/api/prescriptions`

## Seeded Demo Accounts

- Super Admin: `superadmin@test.com` / `admin123`
- Admin: `admin@test.com` / `admin123`
- Receptionist: `staff@test.com` / `staff123`
- Doctors:
  - `alice@test.com` / `doctor123`
  - `bob@test.com` / `doctor123`
  - `charlie@test.com` / `doctor123`
  - `david@test.com` / `doctor123`
  - `eve@test.com` / `doctor123`
- Default hospital code for seeded hospital: `HOSP001`

## Reset and Repair Tooling

- Schema repair script: `tools/schema_repair.sql`
- SQL reset script: `tools/reset_logins.sql`
- JDBC reset utility: `tools/ResetLogins.java`

## Contract Lock Rule

No new backend entities or major endpoint families should be added unless they are required for:

- core demo completion
- mobile integration
- testing/stability
- final report requirements
