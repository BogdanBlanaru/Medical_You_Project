package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.MedicationFrequency;
import com.zega.medical_you_be.model.enums.MedicationLogStatus;
import com.zega.medical_you_be.model.enums.MedicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDto {
    private Long id;
    private Long patientId;
    private Long familyMemberId;
    private String familyMemberName;
    private String name;
    private String dosage;
    private MedicationFrequency frequency;
    private Integer timesPerDay;
    private List<String> specificTimes;
    private String instructions;
    private String prescribedBy;
    private LocalDate startDate;
    private LocalDate endDate;
    private MedicationStatus status;
    private Integer refillReminderDays;
    private Integer pillsRemaining;
    private Integer pillsPerDose;
    private String color;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private boolean currentlyActive;
    private boolean needsRefill;
    private int daysRemaining;
    private double adherenceRate;
    private List<ReminderDto> reminders;
    private List<LogDto> recentLogs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReminderDto {
        private Long id;
        private LocalTime reminderTime;
        private Boolean isEnabled;
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogDto {
        private Long id;
        private LocalDateTime scheduledTime;
        private LocalDateTime takenAt;
        private MedicationLogStatus status;
        private String notes;
        private LocalDateTime createdAt;
    }
}
