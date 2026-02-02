package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.PatientProfileDto;
import com.zega.medical_you_be.model.dto.UpdatePatientProfileDto;
import com.zega.medical_you_be.model.entity.Patient;
import com.zega.medical_you_be.model.entity.PatientProfile;
import com.zega.medical_you_be.repo.PatientProfileRepo;
import com.zega.medical_you_be.repo.PatientRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientProfileServiceImpl implements PatientProfileService {

    private final PatientProfileRepo profileRepo;
    private final PatientRepo patientRepo;

    @Value("${app.upload.dir:uploads/avatars}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public PatientProfileDto getProfileByPatientId(Long patientId) {
        PatientProfile profile = profileRepo.findByPatientIdWithPatient(patientId)
                .orElseGet(() -> createEmptyProfile(patientId));
        return mapToDto(profile);
    }

    @Override
    public PatientProfileDto getProfileByEmail(String email) {
        return profileRepo.findByPatientEmail(email)
                .map(this::mapToDto)
                .orElseGet(() -> {
                    // If no profile exists, find the patient and create an empty profile
                    Patient patient = patientRepo.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("Patient not found for email: " + email));
                    PatientProfile newProfile = createEmptyProfile(patient.getId());
                    return mapToDto(newProfile);
                });
    }

    @Override
    public PatientProfileDto getProfileByMedicalId(String medicalId) {
        return profileRepo.findByMedicalId(medicalId)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Profile not found for medical ID: " + medicalId));
    }

    @Override
    @Transactional
    public PatientProfileDto updateProfile(Long patientId, UpdatePatientProfileDto updateDto) {
        PatientProfile profile = profileRepo.findByPatientId(patientId)
                .orElseGet(() -> createEmptyProfile(patientId));

        // Update personal information
        if (updateDto.getPhoneNumber() != null) {
            profile.setPhoneNumber(updateDto.getPhoneNumber());
        }
        if (updateDto.getDateOfBirth() != null) {
            profile.setDateOfBirth(updateDto.getDateOfBirth());
        }
        if (updateDto.getGender() != null) {
            profile.setGender(updateDto.getGender());
        }
        if (updateDto.getAddress() != null) {
            profile.setAddress(updateDto.getAddress());
        }
        if (updateDto.getCity() != null) {
            profile.setCity(updateDto.getCity());
        }
        if (updateDto.getCountry() != null) {
            profile.setCountry(updateDto.getCountry());
        }

        // Update medical information
        if (updateDto.getBloodType() != null) {
            profile.setBloodType(updateDto.getBloodType());
        }
        if (updateDto.getHeightCm() != null) {
            profile.setHeightCm(updateDto.getHeightCm());
        }
        if (updateDto.getWeightKg() != null) {
            profile.setWeightKg(updateDto.getWeightKg());
        }

        // Update medical arrays using helper methods
        if (updateDto.getAllergies() != null) {
            profile.setAllergiesList(new ArrayList<>(updateDto.getAllergies()));
        }
        if (updateDto.getChronicConditions() != null) {
            profile.setChronicConditionsList(new ArrayList<>(updateDto.getChronicConditions()));
        }
        if (updateDto.getCurrentMedications() != null) {
            List<PatientProfile.Medication> medications = updateDto.getCurrentMedications().stream()
                    .map(dto -> PatientProfile.Medication.builder()
                            .name(dto.getName())
                            .dosage(dto.getDosage())
                            .frequency(dto.getFrequency())
                            .prescribedBy(dto.getPrescribedBy())
                            .startDate(dto.getStartDate())
                            .notes(dto.getNotes())
                            .build())
                    .collect(Collectors.toList());
            profile.setMedicationsList(medications);
        }

        // Update emergency contact
        if (updateDto.getEmergencyContactName() != null) {
            profile.setEmergencyContactName(updateDto.getEmergencyContactName());
        }
        if (updateDto.getEmergencyContactPhone() != null) {
            profile.setEmergencyContactPhone(updateDto.getEmergencyContactPhone());
        }
        if (updateDto.getEmergencyContactRelationship() != null) {
            profile.setEmergencyContactRelationship(updateDto.getEmergencyContactRelationship());
        }

        profile = profileRepo.save(profile);
        return mapToDto(profile);
    }

    @Override
    @Transactional
    public String uploadAvatar(Long patientId, MultipartFile file) {
        PatientProfile profile = profileRepo.findByPatientId(patientId)
                .orElseGet(() -> createEmptyProfile(patientId));

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Delete old avatar if exists
            if (profile.getAvatarUrl() != null) {
                deleteAvatarFile(profile.getAvatarUrl());
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = "avatar_" + patientId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update profile with new avatar URL
            String avatarUrl = baseUrl + "/api/patient/profile/avatar/" + filename;
            profile.setAvatarUrl(avatarUrl);
            profileRepo.save(profile);

            log.info("Avatar uploaded for patient {}: {}", patientId, avatarUrl);
            return avatarUrl;

        } catch (IOException e) {
            log.error("Failed to upload avatar for patient {}", patientId, e);
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteAvatar(Long patientId) {
        PatientProfile profile = profileRepo.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profile.getAvatarUrl() != null) {
            deleteAvatarFile(profile.getAvatarUrl());
            profile.setAvatarUrl(null);
            profileRepo.save(profile);
        }
    }

    @Override
    public MedicalIdCardDto getMedicalIdCard(Long patientId) {
        PatientProfile profile = profileRepo.findByPatientIdWithPatient(patientId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        String dateOfBirthStr = profile.getDateOfBirth() != null
                ? profile.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "N/A";

        String bloodTypeStr = profile.getBloodType() != null
                ? profile.getBloodType().getDisplayName()
                : "Unknown";

        // QR code data contains medical ID for quick lookup
        String qrCodeData = "MEDICAL_ID:" + profile.getMedicalId();

        return new MedicalIdCardDto(
                profile.getMedicalId(),
                profile.getPatient().getName(),
                dateOfBirthStr,
                bloodTypeStr,
                profile.getEmergencyContactName(),
                profile.getEmergencyContactPhone(),
                profile.getAllergiesList(),
                profile.getChronicConditionsList(),
                qrCodeData
        );
    }

    // Helper methods

    private PatientProfile createEmptyProfile(Long patientId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));

        PatientProfile profile = PatientProfile.builder()
                .patient(patient)
                .build();

        // Initialize empty JSON arrays
        profile.setAllergiesList(new ArrayList<>());
        profile.setChronicConditionsList(new ArrayList<>());
        profile.setMedicationsList(new ArrayList<>());

        return profileRepo.save(profile);
    }

    private PatientProfileDto mapToDto(PatientProfile profile) {
        List<PatientProfileDto.MedicationDto> medicationDtos = profile.getMedicationsList().stream()
                .map(med -> PatientProfileDto.MedicationDto.builder()
                        .name(med.getName())
                        .dosage(med.getDosage())
                        .frequency(med.getFrequency())
                        .prescribedBy(med.getPrescribedBy())
                        .startDate(med.getStartDate())
                        .notes(med.getNotes())
                        .build())
                .collect(Collectors.toList());

        return PatientProfileDto.builder()
                .id(profile.getId())
                .patientId(profile.getPatient().getId())
                .patientName(profile.getPatient().getName())
                .patientEmail(profile.getPatient().getEmail())
                .phoneNumber(profile.getPhoneNumber())
                .dateOfBirth(profile.getDateOfBirth())
                .age(profile.getAge())
                .gender(profile.getGender())
                .address(profile.getAddress())
                .city(profile.getCity())
                .country(profile.getCountry())
                .avatarUrl(profile.getAvatarUrl())
                .bloodType(profile.getBloodType())
                .heightCm(profile.getHeightCm())
                .weightKg(profile.getWeightKg())
                .bmi(profile.getBmi())
                .allergies(profile.getAllergiesList())
                .chronicConditions(profile.getChronicConditionsList())
                .currentMedications(medicationDtos)
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactPhone(profile.getEmergencyContactPhone())
                .emergencyContactRelationship(profile.getEmergencyContactRelationship())
                .medicalId(profile.getMedicalId())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private void deleteAvatarFile(String avatarUrl) {
        try {
            // Extract filename from URL
            String filename = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete avatar file: {}", avatarUrl, e);
        }
    }
}
