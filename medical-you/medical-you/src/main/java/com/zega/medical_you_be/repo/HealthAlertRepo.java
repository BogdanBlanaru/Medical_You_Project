package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.HealthAlert;
import com.zega.medical_you_be.model.enums.AlertSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthAlertRepo extends JpaRepository<HealthAlert, Long> {

    // Find all alerts for a patient (through health readings)
    @Query("SELECT ha FROM HealthAlert ha " +
           "JOIN ha.healthReading hr " +
           "WHERE hr.patient.id = :patientId " +
           "ORDER BY ha.createdAt DESC")
    Page<HealthAlert> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);

    // Find unacknowledged alerts for a patient
    @Query("SELECT ha FROM HealthAlert ha " +
           "JOIN ha.healthReading hr " +
           "WHERE hr.patient.id = :patientId " +
           "AND ha.isAcknowledged = false " +
           "ORDER BY ha.createdAt DESC")
    List<HealthAlert> findUnacknowledgedByPatientId(@Param("patientId") Long patientId);

    // Find alerts by severity
    @Query("SELECT ha FROM HealthAlert ha " +
           "JOIN ha.healthReading hr " +
           "WHERE hr.patient.id = :patientId " +
           "AND ha.severity = :severity " +
           "ORDER BY ha.createdAt DESC")
    List<HealthAlert> findByPatientIdAndSeverity(
            @Param("patientId") Long patientId,
            @Param("severity") AlertSeverity severity);

    // Count unacknowledged alerts
    @Query("SELECT COUNT(ha) FROM HealthAlert ha " +
           "JOIN ha.healthReading hr " +
           "WHERE hr.patient.id = :patientId " +
           "AND ha.isAcknowledged = false")
    long countUnacknowledgedByPatientId(@Param("patientId") Long patientId);

    // Count total alerts for a patient
    @Query("SELECT COUNT(ha) FROM HealthAlert ha " +
           "JOIN ha.healthReading hr " +
           "WHERE hr.patient.id = :patientId")
    long countByPatientId(@Param("patientId") Long patientId);

    // Find recent alerts for dashboard
    @Query("SELECT ha FROM HealthAlert ha " +
           "JOIN ha.healthReading hr " +
           "WHERE hr.patient.id = :patientId " +
           "ORDER BY ha.createdAt DESC")
    List<HealthAlert> findRecentByPatientId(@Param("patientId") Long patientId, Pageable pageable);
}
