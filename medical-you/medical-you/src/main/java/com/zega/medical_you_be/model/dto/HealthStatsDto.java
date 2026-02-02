package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.ReadingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatsDto {

    private ReadingType readingType;
    private String unit;
    private BigDecimal latestValue;
    private BigDecimal secondaryLatestValue;
    private LocalDateTime latestMeasuredAt;
    private BigDecimal averageValue;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String trend; // UP, DOWN, STABLE
    private BigDecimal trendPercentage;
    private int totalReadings;
    private int alertsCount;

    // Chart data for the frontend
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDataPoint {
        private LocalDateTime date;
        private BigDecimal value;
        private BigDecimal secondaryValue;
    }

    private List<ChartDataPoint> chartData;

    // Summary of all reading types for dashboard
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardSummary {
        private Map<ReadingType, HealthStatsDto> stats;
        private int totalAlerts;
        private int unacknowledgedAlerts;
        private List<HealthReadingDto.HealthAlertDto> recentAlerts;
    }
}
