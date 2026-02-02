package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.HealthReading;
import com.zega.medical_you_be.model.enums.ReadingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthReadingRepo extends JpaRepository<HealthReading, Long> {

    // Find all readings for a patient
    Page<HealthReading> findByPatientIdOrderByMeasuredAtDesc(Long patientId, Pageable pageable);

    // Find readings by patient and type
    Page<HealthReading> findByPatientIdAndReadingTypeOrderByMeasuredAtDesc(
            Long patientId, ReadingType readingType, Pageable pageable);

    // Find readings for a family member
    Page<HealthReading> findByPatientIdAndFamilyMemberIdOrderByMeasuredAtDesc(
            Long patientId, Long familyMemberId, Pageable pageable);

    // Find readings by patient, family member, and type
    Page<HealthReading> findByPatientIdAndFamilyMemberIdAndReadingTypeOrderByMeasuredAtDesc(
            Long patientId, Long familyMemberId, ReadingType readingType, Pageable pageable);

    // Find readings within a date range
    @Query("SELECT hr FROM HealthReading hr WHERE hr.patient.id = :patientId " +
           "AND (:familyMemberId IS NULL OR hr.familyMember.id = :familyMemberId) " +
           "AND (:readingType IS NULL OR hr.readingType = :readingType) " +
           "AND hr.measuredAt BETWEEN :startDate AND :endDate " +
           "ORDER BY hr.measuredAt DESC")
    List<HealthReading> findByPatientAndDateRange(
            @Param("patientId") Long patientId,
            @Param("familyMemberId") Long familyMemberId,
            @Param("readingType") ReadingType readingType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Get latest reading of each type for a patient
    @Query("SELECT hr FROM HealthReading hr WHERE hr.id IN " +
           "(SELECT MAX(hr2.id) FROM HealthReading hr2 WHERE hr2.patient.id = :patientId " +
           "AND (:familyMemberId IS NULL OR hr2.familyMember.id = :familyMemberId) " +
           "GROUP BY hr2.readingType)")
    List<HealthReading> findLatestByPatientGroupedByType(
            @Param("patientId") Long patientId,
            @Param("familyMemberId") Long familyMemberId);

    // Get latest reading of a specific type
    Optional<HealthReading> findFirstByPatientIdAndReadingTypeOrderByMeasuredAtDesc(
            Long patientId, ReadingType readingType);

    // Get latest reading of a specific type for a family member
    Optional<HealthReading> findFirstByPatientIdAndFamilyMemberIdAndReadingTypeOrderByMeasuredAtDesc(
            Long patientId, Long familyMemberId, ReadingType readingType);

    // Count readings by type for statistics
    @Query("SELECT COUNT(hr) FROM HealthReading hr WHERE hr.patient.id = :patientId " +
           "AND hr.readingType = :readingType " +
           "AND (:familyMemberId IS NULL OR hr.familyMember.id = :familyMemberId)")
    long countByPatientAndType(
            @Param("patientId") Long patientId,
            @Param("readingType") ReadingType readingType,
            @Param("familyMemberId") Long familyMemberId);

    // Average, min, max for statistics
    @Query("SELECT AVG(hr.value), MIN(hr.value), MAX(hr.value) FROM HealthReading hr " +
           "WHERE hr.patient.id = :patientId AND hr.readingType = :readingType " +
           "AND (:familyMemberId IS NULL OR hr.familyMember.id = :familyMemberId) " +
           "AND hr.measuredAt >= :since")
    Object[] getStatsByPatientAndType(
            @Param("patientId") Long patientId,
            @Param("readingType") ReadingType readingType,
            @Param("familyMemberId") Long familyMemberId,
            @Param("since") LocalDateTime since);

    // Get chart data (readings over time)
    @Query("SELECT hr FROM HealthReading hr WHERE hr.patient.id = :patientId " +
           "AND hr.readingType = :readingType " +
           "AND (:familyMemberId IS NULL OR hr.familyMember.id = :familyMemberId) " +
           "AND hr.measuredAt >= :since " +
           "ORDER BY hr.measuredAt ASC")
    List<HealthReading> findChartData(
            @Param("patientId") Long patientId,
            @Param("readingType") ReadingType readingType,
            @Param("familyMemberId") Long familyMemberId,
            @Param("since") LocalDateTime since);
}
