package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.AlertSeverity;
import com.zega.medical_you_be.model.enums.ReadingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReadingDto {
    private Long id;
    private Long patientId;
    private Long familyMemberId;
    private String familyMemberName;
    private ReadingType readingType;
    private BigDecimal value;
    private BigDecimal secondaryValue;
    private String unit;
    private String notes;
    private LocalDateTime measuredAt;
    private LocalDateTime createdAt;
    private String displayValue;
    private List<HealthAlertDto> alerts;
    private boolean hasUnacknowledgedAlerts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthAlertDto {
        private Long id;
        private AlertSeverity severity;
        private String message;
        private Boolean isAcknowledged;
        private LocalDateTime acknowledgedAt;
        private LocalDateTime createdAt;
    }
}
