USE healthcare_db;

-- Repair users.role to support the full Role enum.
ALTER TABLE users
MODIFY COLUMN role ENUM('PATIENT','DOCTOR','ADMIN','RECEPTIONIST','SUPER_ADMIN') NOT NULL;

-- Repair hospitals.status to support the full HospitalStatus enum.
ALTER TABLE hospitals
MODIFY COLUMN status ENUM('PENDING','INSPECTION_SCHEDULED','PHYSICALLY_VERIFIED','APPROVED','REJECTED') NOT NULL;

-- Add physical verification workflow columns when missing.
ALTER TABLE hospitals
ADD COLUMN verification_officer VARCHAR(255) NULL,
ADD COLUMN inspection_scheduled_at DATETIME NULL,
ADD COLUMN physically_verified_at DATETIME NULL,
ADD COLUMN location_verified BIT NULL;
