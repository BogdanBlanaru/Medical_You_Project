package com.zega.medical_you_be.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDto {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime dateTime; // Combined for convenience
    private Boolean isAvailable;
    private String displayTime; // "09:00 AM" format
}
