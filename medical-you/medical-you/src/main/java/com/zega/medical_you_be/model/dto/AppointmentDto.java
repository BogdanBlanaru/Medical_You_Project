package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.AppointmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentDto {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Long patientId;
    private String patientName;
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    private String reason;
    private String notes;
    private LocalDateTime createdAt;
    private Boolean isCancelled;
}
