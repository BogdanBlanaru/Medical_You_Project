package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.ReadingType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHealthReadingDto {

    private Long familyMemberId;

    @NotNull(message = "Reading type is required")
    private ReadingType readingType;

    @NotNull(message = "Value is required")
    private BigDecimal value;

    private BigDecimal secondaryValue;

    private String notes;

    private LocalDateTime measuredAt;
}
