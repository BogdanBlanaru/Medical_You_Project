package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.Medication;
import com.zega.medical_you_be.model.enums.MedicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicationRepo extends JpaRepository<Medication, Long> {

    // Find all medications for a patient
    Page<Medication> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    // Find active medications for a patient
    List<Medication> findByPatientIdAndStatus(Long patientId, MedicationStatus status);

    // Find medications for a family member
    List<Medication> findByPatientIdAndFamilyMemberIdAndStatus(Long patientId, Long familyMemberId, MedicationStatus status);

    // Find all active medications for today's schedule
    @Query("SELECT m FROM Medication m WHERE m.patient.id = :patientId " +
           "AND m.status = 'ACTIVE' " +
           "AND m.startDate <= :today " +
           "AND (m.endDate IS NULL OR m.endDate >= :today)")
    List<Medication> findActiveForToday(@Param("patientId") Long patientId, @Param("today") LocalDate today);

    // Find medications that need refill
    @Query("SELECT m FROM Medication m WHERE m.patient.id = :patientId " +
           "AND m.status = 'ACTIVE' " +
           "AND m.pillsRemaining IS NOT NULL " +
           "AND m.pillsPerDose IS NOT NULL " +
           "AND m.timesPerDay IS NOT NULL " +
           "AND m.refillReminderDays IS NOT NULL " +
           "AND (m.pillsRemaining / (m.pillsPerDose * m.timesPerDay)) <= m.refillReminderDays")
    List<Medication> findNeedingRefill(@Param("patientId") Long patientId);

    // Count active medications
    long countByPatientIdAndStatus(Long patientId, MedicationStatus status);
}
