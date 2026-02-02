package com.zega.medical_you_be.model.dto;

import lombok.*;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorAvailabilityDto {
    private Long id;
    private Long doctorId;
    private Integer dayOfWeek; // 1=Monday to 7=Sunday
    private String dayName; // "Monday", "Tuesday", etc.
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    private Boolean isActive;
}
