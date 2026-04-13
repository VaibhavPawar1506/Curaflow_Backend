package com.healthcare.management_system.dto;

import java.util.List;

public class SymptomBookingRequest {

    private Long patientId;
    private Long hospitalId;
    private List<String> symptoms;
    private String description;
    private Severity userSeverity; // LOW, MEDIUM, HIGH
    private String preferredDate;
    private String preferredShift;

    // Default constructor
    public SymptomBookingRequest() {}

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Severity getUserSeverity() {
        return userSeverity;
    }

    public void setUserSeverity(Severity userSeverity) {
        this.userSeverity = userSeverity;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getPreferredShift() {
        return preferredShift;
    }

    public void setPreferredShift(String preferredShift) {
        this.preferredShift = preferredShift;
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH
    }
}
