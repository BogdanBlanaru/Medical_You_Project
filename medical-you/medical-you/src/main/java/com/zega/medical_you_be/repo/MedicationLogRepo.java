package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.MedicationLog;
import com.zega.medical_you_be.model.enums.MedicationLogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicationLogRepo extends JpaRepository<MedicationLog, Long> {

    // Find logs for a medication
    Page<MedicationLog> findByMedicationIdOrderByCreatedAtDesc(Long medicationId, Pageable pageable);

    // Find recent logs for a medication
    List<MedicationLog> findTop10ByMedicationIdOrderByCreatedAtDesc(Long medicationId);

    // Find logs in a date range
    @Query("SELECT ml FROM MedicationLog ml WHERE ml.medication.id = :medicationId " +
           "AND ml.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ml.createdAt DESC")
    List<MedicationLog> findByMedicationAndDateRange(
            @Param("medicationId") Long medicationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find logs for today for all medications of a patient
    @Query("SELECT ml FROM MedicationLog ml " +
           "JOIN ml.medication m " +
           "WHERE m.patient.id = :patientId " +
           "AND ml.createdAt >= :startOfDay " +
           "ORDER BY ml.createdAt DESC")
    List<MedicationLog> findTodayLogsForPatient(
            @Param("patientId") Long patientId,
            @Param("startOfDay") LocalDateTime startOfDay);

    // Count logs by status for adherence calculation
    @Query("SELECT COUNT(ml) FROM MedicationLog ml WHERE ml.medication.id = :medicationId " +
           "AND ml.status = :status " +
           "AND ml.createdAt >= :since")
    long countByMedicationAndStatusSince(
            @Param("medicationId") Long medicationId,
            @Param("status") MedicationLogStatus status,
            @Param("since") LocalDateTime since);

    // Count total logs for adherence calculation
    @Query("SELECT COUNT(ml) FROM MedicationLog ml WHERE ml.medication.id = :medicationId " +
           "AND ml.createdAt >= :since")
    long countByMedicationSince(
            @Param("medicationId") Long medicationId,
            @Param("since") LocalDateTime since);
}
