package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.PatientProfileDto;
import com.zega.medical_you_be.model.dto.UpdatePatientProfileDto;
import org.springframework.web.multipart.MultipartFile;

public interface PatientProfileService {

    /**
     * Get patient profile by patient ID
     */
    PatientProfileDto getProfileByPatientId(Long patientId);

    /**
     * Get patient profile by email
     */
    PatientProfileDto getProfileByEmail(String email);

    /**
     * Get patient profile by medical ID
     */
    PatientProfileDto getProfileByMedicalId(String medicalId);

    /**
     * Create or update patient profile
     */
    PatientProfileDto updateProfile(Long patientId, UpdatePatientProfileDto updateDto);

    /**
     * Upload patient avatar
     */
    String uploadAvatar(Long patientId, MultipartFile file);

    /**
     * Delete patient avatar
     */
    void deleteAvatar(Long patientId);

    /**
     * Get medical ID card data
     */
    MedicalIdCardDto getMedicalIdCard(Long patientId);

    /**
     * DTO for Medical ID Card
     */
    record MedicalIdCardDto(
        String medicalId,
        String patientName,
        String dateOfBirth,
        String bloodType,
        String emergencyContact,
        String emergencyPhone,
        java.util.List<String> allergies,
        java.util.List<String> chronicConditions,
        String qrCodeData
    ) {}
}
