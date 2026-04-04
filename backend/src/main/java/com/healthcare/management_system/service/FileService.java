package com.healthcare.management_system.service;

import com.healthcare.management_system.entity.User;
import com.healthcare.management_system.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir:uploads/prescriptions}")
    private String uploadDir;

    public String savePrescription(User user, MultipartFile file) throws IOException {
        Long hospitalId = requireHospitalScope(user);
        Path root = Paths.get(uploadDir);
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        String originalName = file.getOriginalFilename() == null ? "prescription" : Paths.get(file.getOriginalFilename()).getFileName().toString();
        String sanitizedName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = "hospital-" + hospitalId + "__" + UUID.randomUUID() + "_" + sanitizedName;
        Files.copy(file.getInputStream(), root.resolve(fileName));

        return fileName;
    }

    public Path getPrescriptionPath(User user, String fileName) {
        if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new AccessDeniedException("Invalid prescription file");
        }
        if (user == null) {
            throw new AccessDeniedException("Access denied");
        }
        if (user.getRole() != Role.SUPER_ADMIN) {
            Long hospitalId = requireHospitalScope(user);
            String expectedPrefix = "hospital-" + hospitalId + "__";
            if (!fileName.startsWith(expectedPrefix)) {
                throw new AccessDeniedException("You are not authorized to access another hospital's prescription files");
            }
        }
        return Paths.get(uploadDir).resolve(fileName);
    }

    private Long requireHospitalScope(User user) {
        if (user == null || user.getHospital() == null) {
            throw new AccessDeniedException("Hospital context is required");
        }
        return user.getHospital().getId();
    }
}
