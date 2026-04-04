package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.HospitalDirectoryResponse;
import com.healthcare.management_system.entity.Hospital;
import com.healthcare.management_system.enums.HospitalStatus;
import com.healthcare.management_system.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public List<HospitalDirectoryResponse> getApprovedHospitals() {
        return hospitalRepository.findByStatusOrderByNameAsc(HospitalStatus.APPROVED).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private HospitalDirectoryResponse mapToResponse(Hospital hospital) {
        return HospitalDirectoryResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .hospitalCode(hospital.getHospitalCode())
                .address(hospital.getAddress())
                .build();
    }
}
