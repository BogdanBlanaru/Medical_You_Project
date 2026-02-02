package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.BloodType;
import com.zega.medical_you_be.model.enums.Gender;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfileDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;

    // Personal Information
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Integer age;
    private Gender gender;
    private String address;
    private String city;
    private String country;
    private String avatarUrl;

    // Medical Information
    private BloodType bloodType;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private BigDecimal bmi;

    // Medical Arrays
    private List<String> allergies;
    private List<String> chronicConditions;
    private List<MedicationDto> currentMedications;

    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;

    // Medical ID
    private String medicalId;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MedicationDto {
        private String name;
        private String dosage;
        private String frequency;
        private String prescribedBy;
        private LocalDate startDate;
        private String notes;
    }
}
