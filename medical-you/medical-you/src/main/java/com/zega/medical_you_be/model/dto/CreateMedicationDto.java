package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.MedicationFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicationDto {

    private Long familyMemberId;

    @NotBlank(message = "Medication name is required")
    private String name;

    private String dosage;

    @NotNull(message = "Frequency is required")
    private MedicationFrequency frequency;

    private Integer timesPerDay;

    private List<String> specificTimes;

    private String instructions;

    private String prescribedBy;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private Integer refillReminderDays;

    private Integer pillsRemaining;

    private Integer pillsPerDose;

    private String color;

    private String notes;
}
