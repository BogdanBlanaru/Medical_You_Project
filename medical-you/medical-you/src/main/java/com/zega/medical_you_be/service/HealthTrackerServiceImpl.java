package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.CreateHealthReadingDto;
import com.zega.medical_you_be.model.dto.HealthReadingDto;
import com.zega.medical_you_be.model.dto.HealthStatsDto;
import com.zega.medical_you_be.model.entity.FamilyMember;
import com.zega.medical_you_be.model.entity.HealthAlert;
import com.zega.medical_you_be.model.entity.HealthReading;
import com.zega.medical_you_be.model.entity.Patient;
import com.zega.medical_you_be.model.enums.ReadingType;
import com.zega.medical_you_be.repo.FamilyMemberRepo;
import com.zega.medical_you_be.repo.HealthAlertRepo;
import com.zega.medical_you_be.repo.HealthReadingRepo;
import com.zega.medical_you_be.repo.PatientRepo;
import com.zega.medical_you_be.util.HealthRangeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthTrackerServiceImpl implements HealthTrackerService {

    private final HealthReadingRepo healthReadingRepo;
    private final HealthAlertRepo healthAlertRepo;
    private final PatientRepo patientRepo;
    private final FamilyMemberRepo familyMemberRepo;
    private final HealthRangeValidator healthRangeValidator;

    // ==================== CRUD Operations ====================

    @Override
    @Transactional
    public HealthReadingDto createReading(String userEmail, CreateHealthReadingDto dto) {
        Patient patient = getPatientByEmail(userEmail);

        FamilyMember familyMember = null;
        if (dto.getFamilyMemberId() != null) {
            familyMember = familyMemberRepo.findById(dto.getFamilyMemberId())
                    .orElseThrow(() -> new RuntimeException("Family member not found"));
            // Verify ownership
            if (!familyMember.getFamilyGroup().getCreatedBy().getId().equals(patient.getId())) {
                throw new RuntimeException("Access denied to this family member");
            }
        }

        // Create reading
        HealthReading reading = HealthReading.builder()
                .patient(patient)
                .familyMember(familyMember)
                .readingType(dto.getReadingType())
                .value(dto.getValue())
                .secondaryValue(dto.getSecondaryValue())
                .unit(healthRangeValidator.getUnitForType(dto.getReadingType()))
                .notes(dto.getNotes())
                .measuredAt(dto.getMeasuredAt() != null ? dto.getMeasuredAt() : LocalDateTime.now())
                .build();

        reading = healthReadingRepo.save(reading);

        // Validate and create alerts
        List<HealthAlert> alerts = healthRangeValidator.validateReading(reading);
        if (!alerts.isEmpty()) {
            for (HealthAlert alert : alerts) {
                healthAlertRepo.save(alert);
            }
            reading.setAlerts(alerts);
            log.info("Created {} alert(s) for reading {} (type: {})", alerts.size(), reading.getId(), reading.getReadingType());
        }

        log.info("Health reading created: id={}, type={}, value={}", reading.getId(), reading.getReadingType(), reading.getValue());
        return mapToDto(reading);
    }

    @Override
    public HealthReadingDto getReading(String userEmail, Long readingId) {
        Patient patient = getPatientByEmail(userEmail);
        HealthReading reading = healthReadingRepo.findById(readingId)
                .orElseThrow(() -> new RuntimeException("Reading not found"));

        if (!reading.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToDto(reading);
    }

    @Override
    @Transactional
    public HealthReadingDto updateReading(String userEmail, Long readingId, CreateHealthReadingDto dto) {
        Patient patient = getPatientByEmail(userEmail);
        HealthReading reading = healthReadingRepo.findById(readingId)
                .orElseThrow(() -> new RuntimeException("Reading not found"));

        if (!reading.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        // Update fields
        reading.setValue(dto.getValue());
        reading.setSecondaryValue(dto.getSecondaryValue());
        reading.setNotes(dto.getNotes());
        if (dto.getMeasuredAt() != null) {
            reading.setMeasuredAt(dto.getMeasuredAt());
        }

        // Clear old alerts and revalidate
        reading.getAlerts().clear();
        List<HealthAlert> newAlerts = healthRangeValidator.validateReading(reading);
        for (HealthAlert alert : newAlerts) {
            healthAlertRepo.save(alert);
        }
        reading.setAlerts(newAlerts);

        reading = healthReadingRepo.save(reading);
        return mapToDto(reading);
    }

    @Override
    @Transactional
    public void deleteReading(String userEmail, Long readingId) {
        Patient patient = getPatientByEmail(userEmail);
        HealthReading reading = healthReadingRepo.findById(readingId)
                .orElseThrow(() -> new RuntimeException("Reading not found"));

        if (!reading.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        healthReadingRepo.delete(reading);
        log.info("Health reading deleted: id={}", readingId);
    }

    // ==================== List Readings ====================

    @Override
    public Page<HealthReadingDto> getReadings(String userEmail, Long familyMemberId, ReadingType type, Pageable pageable) {
        Patient patient = getPatientByEmail(userEmail);

        Page<HealthReading> readings;
        if (familyMemberId != null && type != null) {
            readings = healthReadingRepo.findByPatientIdAndFamilyMemberIdAndReadingTypeOrderByMeasuredAtDesc(
                    patient.getId(), familyMemberId, type, pageable);
        } else if (familyMemberId != null) {
            readings = healthReadingRepo.findByPatientIdAndFamilyMemberIdOrderByMeasuredAtDesc(
                    patient.getId(), familyMemberId, pageable);
        } else if (type != null) {
            readings = healthReadingRepo.findByPatientIdAndReadingTypeOrderByMeasuredAtDesc(
                    patient.getId(), type, pageable);
        } else {
            readings = healthReadingRepo.findByPatientIdOrderByMeasuredAtDesc(patient.getId(), pageable);
        }

        return readings.map(this::mapToDto);
    }

    @Override
    public List<HealthReadingDto> getReadingsByDateRange(String userEmail, Long familyMemberId, ReadingType type,
                                                          LocalDateTime startDate, LocalDateTime endDate) {
        Patient patient = getPatientByEmail(userEmail);
        List<HealthReading> readings = healthReadingRepo.findByPatientAndDateRange(
                patient.getId(), familyMemberId, type, startDate, endDate);
        return readings.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // ==================== Statistics ====================

    @Override
    public HealthStatsDto getStats(String userEmail, Long familyMemberId, ReadingType type, int days) {
        Patient patient = getPatientByEmail(userEmail);
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        // Get statistics - handle potential array wrapping from JPA
        Object[] stats = healthReadingRepo.getStatsByPatientAndType(patient.getId(), type, familyMemberId, since);
        BigDecimal avg = null, min = null, max = null;

        if (stats != null && stats.length > 0) {
            // Check if result is wrapped in outer array (some JPA drivers do this)
            Object firstElement = stats[0];
            if (firstElement instanceof Object[]) {
                Object[] innerStats = (Object[]) firstElement;
                avg = innerStats.length > 0 ? toBigDecimal(innerStats[0]) : null;
                min = innerStats.length > 1 ? toBigDecimal(innerStats[1]) : null;
                max = innerStats.length > 2 ? toBigDecimal(innerStats[2]) : null;
            } else {
                // Normal case - flat array with 3 elements
                avg = toBigDecimal(stats[0]);
                min = stats.length > 1 ? toBigDecimal(stats[1]) : null;
                max = stats.length > 2 ? toBigDecimal(stats[2]) : null;
            }
        }

        // Get latest reading
        Optional<HealthReading> latestOpt = familyMemberId != null ?
                healthReadingRepo.findFirstByPatientIdAndFamilyMemberIdAndReadingTypeOrderByMeasuredAtDesc(patient.getId(), familyMemberId, type) :
                healthReadingRepo.findFirstByPatientIdAndReadingTypeOrderByMeasuredAtDesc(patient.getId(), type);

        // Get chart data
        List<HealthReading> chartReadings = healthReadingRepo.findChartData(patient.getId(), type, familyMemberId, since);
        List<HealthStatsDto.ChartDataPoint> chartData = chartReadings.stream()
                .map(r -> HealthStatsDto.ChartDataPoint.builder()
                        .date(r.getMeasuredAt())
                        .value(r.getValue())
                        .secondaryValue(r.getSecondaryValue())
                        .build())
                .collect(Collectors.toList());

        // Calculate trend
        String trend = "STABLE";
        BigDecimal trendPercentage = BigDecimal.ZERO;
        if (chartData.size() >= 2) {
            BigDecimal first = chartData.get(0).getValue();
            BigDecimal last = chartData.get(chartData.size() - 1).getValue();
            if (first != null && last != null && first.compareTo(BigDecimal.ZERO) != 0) {
                trendPercentage = last.subtract(first).divide(first, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                if (trendPercentage.compareTo(BigDecimal.valueOf(5)) > 0) {
                    trend = "UP";
                } else if (trendPercentage.compareTo(BigDecimal.valueOf(-5)) < 0) {
                    trend = "DOWN";
                }
            }
        }

        long totalReadings = healthReadingRepo.countByPatientAndType(patient.getId(), type, familyMemberId);

        return HealthStatsDto.builder()
                .readingType(type)
                .unit(healthRangeValidator.getUnitForType(type))
                .latestValue(latestOpt.map(HealthReading::getValue).orElse(null))
                .secondaryLatestValue(latestOpt.map(HealthReading::getSecondaryValue).orElse(null))
                .latestMeasuredAt(latestOpt.map(HealthReading::getMeasuredAt).orElse(null))
                .averageValue(avg)
                .minValue(min)
                .maxValue(max)
                .trend(trend)
                .trendPercentage(trendPercentage.setScale(1, RoundingMode.HALF_UP))
                .totalReadings((int) totalReadings)
                .chartData(chartData)
                .build();
    }

    @Override
    public HealthStatsDto.DashboardSummary getDashboardSummary(String userEmail, Long familyMemberId) {
        Patient patient = getPatientByEmail(userEmail);

        // Get stats for all reading types
        Map<ReadingType, HealthStatsDto> statsMap = new EnumMap<>(ReadingType.class);
        for (ReadingType type : ReadingType.values()) {
            HealthStatsDto stats = getStats(userEmail, familyMemberId, type, 30);
            if (stats.getTotalReadings() > 0) {
                statsMap.put(type, stats);
            }
        }

        // Get alert counts
        long totalAlerts = healthAlertRepo.countByPatientId(patient.getId());
        long unacknowledgedAlerts = healthAlertRepo.countUnacknowledgedByPatientId(patient.getId());

        // Get recent alerts
        List<HealthAlert> recentAlerts = healthAlertRepo.findRecentByPatientId(patient.getId(), PageRequest.of(0, 5));
        List<HealthReadingDto.HealthAlertDto> alertDtos = recentAlerts.stream()
                .map(this::mapAlertToDto)
                .collect(Collectors.toList());

        return HealthStatsDto.DashboardSummary.builder()
                .stats(statsMap)
                .totalAlerts((int) totalAlerts)
                .unacknowledgedAlerts((int) unacknowledgedAlerts)
                .recentAlerts(alertDtos)
                .build();
    }

    @Override
    public List<HealthStatsDto.ChartDataPoint> getChartData(String userEmail, Long familyMemberId, ReadingType type, int days) {
        Patient patient = getPatientByEmail(userEmail);
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        List<HealthReading> readings = healthReadingRepo.findChartData(patient.getId(), type, familyMemberId, since);
        return readings.stream()
                .map(r -> HealthStatsDto.ChartDataPoint.builder()
                        .date(r.getMeasuredAt())
                        .value(r.getValue())
                        .secondaryValue(r.getSecondaryValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== Alerts ====================

    @Override
    public List<HealthReadingDto.HealthAlertDto> getAlerts(String userEmail, boolean unacknowledgedOnly) {
        Patient patient = getPatientByEmail(userEmail);

        List<HealthAlert> alerts = unacknowledgedOnly ?
                healthAlertRepo.findUnacknowledgedByPatientId(patient.getId()) :
                healthAlertRepo.findByPatientId(patient.getId(), PageRequest.of(0, 100)).getContent();

        return alerts.stream().map(this::mapAlertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void acknowledgeAlert(String userEmail, Long alertId) {
        Patient patient = getPatientByEmail(userEmail);
        HealthAlert alert = healthAlertRepo.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getHealthReading().getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        alert.acknowledge();
        healthAlertRepo.save(alert);
        log.info("Alert {} acknowledged by patient {}", alertId, patient.getId());
    }

    @Override
    public long getUnacknowledgedAlertsCount(String userEmail) {
        Patient patient = getPatientByEmail(userEmail);
        return healthAlertRepo.countUnacknowledgedByPatientId(patient.getId());
    }

    // ==================== Helper Methods ====================

    private Patient getPatientByEmail(String email) {
        return patientRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    private HealthReadingDto mapToDto(HealthReading reading) {
        return HealthReadingDto.builder()
                .id(reading.getId())
                .patientId(reading.getPatient().getId())
                .familyMemberId(reading.getFamilyMember() != null ? reading.getFamilyMember().getId() : null)
                .familyMemberName(reading.getFamilyMember() != null ? reading.getFamilyMember().getName() : null)
                .readingType(reading.getReadingType())
                .value(reading.getValue())
                .secondaryValue(reading.getSecondaryValue())
                .unit(reading.getUnit())
                .notes(reading.getNotes())
                .measuredAt(reading.getMeasuredAt())
                .createdAt(reading.getCreatedAt())
                .displayValue(reading.getDisplayValue())
                .alerts(reading.getAlerts().stream().map(this::mapAlertToDto).collect(Collectors.toList()))
                .hasUnacknowledgedAlerts(reading.hasUnacknowledgedAlerts())
                .build();
    }

    private HealthReadingDto.HealthAlertDto mapAlertToDto(HealthAlert alert) {
        return HealthReadingDto.HealthAlertDto.builder()
                .id(alert.getId())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .isAcknowledged(alert.getIsAcknowledged())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    /**
     * Safely converts a database aggregate result to BigDecimal.
     * Handles null values and various numeric types returned by different database drivers.
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(1, RoundingMode.HALF_UP);
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue()).setScale(1, RoundingMode.HALF_UP);
        }
        try {
            return new BigDecimal(value.toString()).setScale(1, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse value to BigDecimal: {}", value);
            return null;
        }
    }
}
