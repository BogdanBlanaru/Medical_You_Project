package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.entity.PatientProfile;
import com.zega.medical_you_be.model.enums.BloodType;
import com.zega.medical_you_be.model.enums.Gender;
import com.zega.medical_you_be.model.enums.RelationshipType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependentProfileDto {
    private Long id;
    private Long familyMemberId;

    // Family member info
    private String name;
    private RelationshipType relationshipType;
    private LocalDate dateOfBirth;
    private Integer age;

    // Personal Information
    private String phoneNumber;
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

    // Medical data
    private List<String> allergies;
    private List<String> chronicConditions;
    private List<PatientProfile.Medication> medications;

    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;

    // Medical ID
    private String medicalId;
}
