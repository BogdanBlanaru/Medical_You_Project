package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.CreateHealthReadingDto;
import com.zega.medical_you_be.model.dto.HealthReadingDto;
import com.zega.medical_you_be.model.dto.HealthStatsDto;
import com.zega.medical_you_be.model.enums.ReadingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface HealthTrackerService {

    // CRUD operations for readings
    HealthReadingDto createReading(String userEmail, CreateHealthReadingDto dto);

    HealthReadingDto getReading(String userEmail, Long readingId);

    HealthReadingDto updateReading(String userEmail, Long readingId, CreateHealthReadingDto dto);

    void deleteReading(String userEmail, Long readingId);

    // List readings
    Page<HealthReadingDto> getReadings(String userEmail, Long familyMemberId, ReadingType type, Pageable pageable);

    List<HealthReadingDto> getReadingsByDateRange(String userEmail, Long familyMemberId, ReadingType type,
                                                   LocalDateTime startDate, LocalDateTime endDate);

    // Statistics
    HealthStatsDto getStats(String userEmail, Long familyMemberId, ReadingType type, int days);

    HealthStatsDto.DashboardSummary getDashboardSummary(String userEmail, Long familyMemberId);

    List<HealthStatsDto.ChartDataPoint> getChartData(String userEmail, Long familyMemberId, ReadingType type, int days);

    // Alerts
    List<HealthReadingDto.HealthAlertDto> getAlerts(String userEmail, boolean unacknowledgedOnly);

    void acknowledgeAlert(String userEmail, Long alertId);

    long getUnacknowledgedAlertsCount(String userEmail);
}
