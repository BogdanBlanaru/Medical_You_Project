package com.zega.medical_you_be.controller;

import com.zega.medical_you_be.model.dto.CreateMedicationDto;
import com.zega.medical_you_be.model.dto.MedicationDto;
import com.zega.medical_you_be.service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MedicationController {

    private final MedicationService medicationService;

    // ==================== CRUD Endpoints ====================

    @PostMapping
    public ResponseEntity<MedicationDto> createMedication(
            Authentication auth,
            @Valid @RequestBody CreateMedicationDto dto) {
        log.info("Creating medication: {}", dto.getName());
        MedicationDto result = medicationService.createMedication(auth.getName(), dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationDto> getMedication(
            Authentication auth,
            @PathVariable Long id) {
        MedicationDto result = medicationService.getMedication(auth.getName(), id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationDto> updateMedication(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody CreateMedicationDto dto) {
        MedicationDto result = medicationService.updateMedication(auth.getName(), id, dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(
            Authentication auth,
            @PathVariable Long id) {
        medicationService.deleteMedication(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // ==================== List Endpoints ====================

    @GetMapping
    public ResponseEntity<Page<MedicationDto>> getMedications(
            Authentication auth,
            @RequestParam(required = false) Long familyMemberId,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MedicationDto> result = medicationService.getMedications(auth.getName(), familyMemberId, activeOnly, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/today")
    public ResponseEntity<List<MedicationDto>> getTodaySchedule(
            Authentication auth,
            @RequestParam(required = false) Long familyMemberId) {
        List<MedicationDto> result = medicationService.getTodaySchedule(auth.getName(), familyMemberId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/refill")
    public ResponseEntity<List<MedicationDto>> getMedicationsNeedingRefill(Authentication auth) {
        List<MedicationDto> result = medicationService.getMedicationsNeedingRefill(auth.getName());
        return ResponseEntity.ok(result);
    }

    // ==================== Logging Endpoints ====================

    @PostMapping("/{id}/take")
    public ResponseEntity<MedicationDto.LogDto> takeMedication(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        MedicationDto.LogDto result = medicationService.logMedicationTaken(auth.getName(), id, notes);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/skip")
    public ResponseEntity<MedicationDto.LogDto> skipMedication(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        MedicationDto.LogDto result = medicationService.logMedicationSkipped(auth.getName(), id, notes);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<MedicationDto.LogDto>> getMedicationLogs(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam(defaultValue = "30") int days) {
        List<MedicationDto.LogDto> result = medicationService.getMedicationLogs(auth.getName(), id, days);
        return ResponseEntity.ok(result);
    }

    // ==================== Status Management ====================

    @PostMapping("/{id}/pause")
    public ResponseEntity<MedicationDto> pauseMedication(
            Authentication auth,
            @PathVariable Long id) {
        MedicationDto result = medicationService.pauseMedication(auth.getName(), id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<MedicationDto> resumeMedication(
            Authentication auth,
            @PathVariable Long id) {
        MedicationDto result = medicationService.resumeMedication(auth.getName(), id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<MedicationDto> completeMedication(
            Authentication auth,
            @PathVariable Long id) {
        MedicationDto result = medicationService.completeMedication(auth.getName(), id);
        return ResponseEntity.ok(result);
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<MedicationService.MedicationDashboard> getDashboard(
            Authentication auth,
            @RequestParam(required = false) Long familyMemberId) {
        MedicationService.MedicationDashboard result = medicationService.getDashboard(auth.getName(), familyMemberId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/adherence")
    public ResponseEntity<Double> getAdherenceRate(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam(defaultValue = "30") int days) {
        double result = medicationService.getAdherenceRate(auth.getName(), id, days);
        return ResponseEntity.ok(result);
    }
}
