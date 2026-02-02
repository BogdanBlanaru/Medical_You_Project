package com.zega.medical_you_be.model.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zega.medical_you_be.model.enums.BloodType;
import com.zega.medical_you_be.model.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dependent_profiles")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DependentProfile {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id", nullable = false, unique = true)
    private FamilyMember familyMember;

    // Personal Information
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // Medical Information
    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", length = 10)
    private BloodType bloodType;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    // JSON Arrays stored as TEXT
    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    private String chronicConditions;

    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;

    // Emergency Contact
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relationship", length = 50)
    private String emergencyContactRelationship;

    // Medical ID
    @Column(name = "medical_id", length = 20, unique = true)
    private String medicalId;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Generate unique medical ID
    @PrePersist
    public void generateMedicalId() {
        if (this.medicalId == null) {
            // Format: FAM-XXXXXXXX (8 random alphanumeric characters)
            this.medicalId = "FAM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    // Helper method to calculate BMI
    public BigDecimal getBmi() {
        if (heightCm == null || weightKg == null ||
            heightCm.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        return weightKg.divide(heightM.multiply(heightM), 2, java.math.RoundingMode.HALF_UP);
    }

    // JSON Helper methods for allergies
    public List<String> getAllergiesList() {
        if (allergies == null || allergies.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(allergies, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setAllergiesList(List<String> list) {
        try {
            this.allergies = objectMapper.writeValueAsString(list != null ? list : new ArrayList<>());
        } catch (JsonProcessingException e) {
            this.allergies = "[]";
        }
    }

    // JSON Helper methods for chronic conditions
    public List<String> getChronicConditionsList() {
        if (chronicConditions == null || chronicConditions.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(chronicConditions, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setChronicConditionsList(List<String> list) {
        try {
            this.chronicConditions = objectMapper.writeValueAsString(list != null ? list : new ArrayList<>());
        } catch (JsonProcessingException e) {
            this.chronicConditions = "[]";
        }
    }

    // JSON Helper methods for medications (reuse Medication class from PatientProfile)
    public List<PatientProfile.Medication> getMedicationsList() {
        if (currentMedications == null || currentMedications.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(currentMedications, new TypeReference<List<PatientProfile.Medication>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setMedicationsList(List<PatientProfile.Medication> list) {
        try {
            this.currentMedications = objectMapper.writeValueAsString(list != null ? list : new ArrayList<>());
        } catch (JsonProcessingException e) {
            this.currentMedications = "[]";
        }
    }
}
