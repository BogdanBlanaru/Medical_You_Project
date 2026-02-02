package com.zega.medical_you_be.controller;

import com.zega.medical_you_be.model.dto.CreateHealthReadingDto;
import com.zega.medical_you_be.model.dto.HealthReadingDto;
import com.zega.medical_you_be.model.dto.HealthStatsDto;
import com.zega.medical_you_be.model.enums.ReadingType;
import com.zega.medical_you_be.service.HealthTrackerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HealthTrackerController {

    private final HealthTrackerService healthTrackerService;

    // ==================== CRUD Endpoints ====================

    @PostMapping("/readings")
    public ResponseEntity<HealthReadingDto> createReading(
            Authentication auth,
            @Valid @RequestBody CreateHealthReadingDto dto) {
        log.info("Creating health reading: type={}, value={}", dto.getReadingType(), dto.getValue());
        HealthReadingDto result = healthTrackerService.createReading(auth.getName(), dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/readings/{id}")
    public ResponseEntity<HealthReadingDto> getReading(
            Authentication auth,
            @PathVariable Long id) {
        HealthReadingDto result = healthTrackerService.getReading(auth.getName(), id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/readings/{id}")
    public ResponseEntity<HealthReadingDto> updateReading(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody CreateHealthReadingDto dto) {
        HealthReadingDto result = healthTrackerService.updateReading(auth.getName(), id, dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/readings/{id}")
    public ResponseEntity<Void> deleteReading(
            Authentication auth,
            @PathVariable Long id) {
        healthTrackerService.deleteReading(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // ==================== List Endpoints ====================

    @GetMapping("/readings")
    public ResponseEntity<Page<HealthReadingDto>> getReadings(
            Authentication auth,
            @RequestParam(required = false) Long familyMemberId,
            @RequestParam(required = false) ReadingType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "measuredAt"));
        Page<HealthReadingDto> result = healthTrackerService.getReadings(auth.getName(), familyMemberId, type, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/readings/range")
    public ResponseEntity<List<HealthReadingDto>> getReadingsByDateRange(
            Authentication auth,
            @RequestParam(required = false) Long familyMemberId,
            @RequestParam(required = false) ReadingType type,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<HealthReadingDto> result = healthTrackerService.getReadingsByDateRange(
                auth.getName(), familyMemberId, type, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    // ==================== Statistics Endpoints ====================

    @GetMapping("/stats/{type}")
    public ResponseEntity<HealthStatsDto> getStats(
            Authentication auth,
            @PathVariable ReadingType type,
            @RequestParam(required = false) Long familyMemberId,
            @RequestParam(defaultValue = "30") int days) {
        HealthStatsDto result = healthTrackerService.getStats(auth.getName(), familyMemberId, type, days);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<HealthStatsDto.DashboardSummary> getDashboardSummary(
            Authentication auth,
            @RequestParam(required = false) Long familyMemberId) {
        HealthStatsDto.DashboardSummary result = healthTrackerService.getDashboardSummary(auth.getName(), familyMemberId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chart/{type}")
    public ResponseEntity<List<HealthStatsDto.ChartDataPoint>> getChartData(
            Authentication auth,
            @PathVariable ReadingType type,
            @RequestParam(required = false) Long familyMemberId,
            @RequestParam(defaultValue = "30") int days) {
        List<HealthStatsDto.ChartDataPoint> result = healthTrackerService.getChartData(
                auth.getName(), familyMemberId, type, days);
        return ResponseEntity.ok(result);
    }

    // ==================== Alerts Endpoints ====================

    @GetMapping("/alerts")
    public ResponseEntity<List<HealthReadingDto.HealthAlertDto>> getAlerts(
            Authentication auth,
            @RequestParam(defaultValue = "false") boolean unacknowledgedOnly) {
        List<HealthReadingDto.HealthAlertDto> result = healthTrackerService.getAlerts(auth.getName(), unacknowledgedOnly);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<Void> acknowledgeAlert(
            Authentication auth,
            @PathVariable Long id) {
        healthTrackerService.acknowledgeAlert(auth.getName(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/alerts/count")
    public ResponseEntity<Long> getUnacknowledgedAlertsCount(Authentication auth) {
        long count = healthTrackerService.getUnacknowledgedAlertsCount(auth.getName());
        return ResponseEntity.ok(count);
    }

    // ==================== Reading Types Info ====================

    @GetMapping("/types")
    public ResponseEntity<List<ReadingTypeInfo>> getReadingTypes() {
        List<ReadingTypeInfo> types = List.of(
                new ReadingTypeInfo(ReadingType.BLOOD_GLUCOSE, "Blood Glucose", "mg/dL", "Glicemie", true, false),
                new ReadingTypeInfo(ReadingType.BLOOD_PRESSURE, "Blood Pressure", "mmHg", "Tensiune arterială", true, true),
                new ReadingTypeInfo(ReadingType.HEART_RATE, "Heart Rate", "bpm", "Puls", true, false),
                new ReadingTypeInfo(ReadingType.WEIGHT, "Weight", "kg", "Greutate", true, false),
                new ReadingTypeInfo(ReadingType.TEMPERATURE, "Temperature", "°C", "Temperatură", true, false),
                new ReadingTypeInfo(ReadingType.OXYGEN_SATURATION, "Oxygen Saturation", "%", "Saturație oxigen", true, false),
                new ReadingTypeInfo(ReadingType.BMI, "Body Mass Index", "", "Indice masă corporală", true, false),
                new ReadingTypeInfo(ReadingType.CHOLESTEROL, "Cholesterol", "mg/dL", "Colesterol", true, false),
                new ReadingTypeInfo(ReadingType.STEPS, "Steps", "steps", "Pași", true, false)
        );
        return ResponseEntity.ok(types);
    }

    // Helper record for reading type info
    public record ReadingTypeInfo(
            ReadingType type,
            String name,
            String unit,
            String nameRo,
            boolean supportsManualEntry,
            boolean hasSecondaryValue
    ) {}
}
