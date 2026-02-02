package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.BloodType;
import com.zega.medical_you_be.model.enums.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePatientProfileDto {

    // Personal Information
    @Pattern(regexp = "^[+]?[0-9\\s-]{7,20}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;

    // Medical Information
    private BloodType bloodType;

    @Positive(message = "Height must be positive")
    private BigDecimal heightCm;

    @Positive(message = "Weight must be positive")
    private BigDecimal weightKg;

    // Medical Arrays
    private List<String> allergies;
    private List<String> chronicConditions;
    private List<PatientProfileDto.MedicationDto> currentMedications;

    // Emergency Contact
    @Size(max = 255, message = "Emergency contact name must be less than 255 characters")
    private String emergencyContactName;

    @Pattern(regexp = "^[+]?[0-9\\s-]{7,20}$", message = "Invalid emergency contact phone format")
    private String emergencyContactPhone;

    @Size(max = 50, message = "Relationship must be less than 50 characters")
    private String emergencyContactRelationship;
}
