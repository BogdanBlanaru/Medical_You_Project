package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.CreateMedicationDto;
import com.zega.medical_you_be.model.dto.MedicationDto;
import com.zega.medical_you_be.model.enums.MedicationLogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MedicationService {

    // CRUD
    MedicationDto createMedication(String userEmail, CreateMedicationDto dto);
    MedicationDto getMedication(String userEmail, Long medicationId);
    MedicationDto updateMedication(String userEmail, Long medicationId, CreateMedicationDto dto);
    void deleteMedication(String userEmail, Long medicationId);

    // List operations
    Page<MedicationDto> getMedications(String userEmail, Long familyMemberId, boolean activeOnly, Pageable pageable);
    List<MedicationDto> getTodaySchedule(String userEmail, Long familyMemberId);
    List<MedicationDto> getMedicationsNeedingRefill(String userEmail);

    // Medication logging
    MedicationDto.LogDto logMedicationTaken(String userEmail, Long medicationId, String notes);
    MedicationDto.LogDto logMedicationSkipped(String userEmail, Long medicationId, String notes);
    List<MedicationDto.LogDto> getMedicationLogs(String userEmail, Long medicationId, int days);

    // Status management
    MedicationDto pauseMedication(String userEmail, Long medicationId);
    MedicationDto resumeMedication(String userEmail, Long medicationId);
    MedicationDto completeMedication(String userEmail, Long medicationId);

    // Stats
    double getAdherenceRate(String userEmail, Long medicationId, int days);
    MedicationDashboard getDashboard(String userEmail, Long familyMemberId);

    // Dashboard summary object
    record MedicationDashboard(
        int activeMedications,
        int takenToday,
        int remainingToday,
        int medicationsNeedingRefill,
        double overallAdherence,
        List<MedicationDto> todaySchedule,
        List<MedicationDto> needRefill
    ) {}
}
