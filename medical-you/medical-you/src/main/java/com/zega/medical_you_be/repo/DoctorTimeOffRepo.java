package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.DoctorTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DoctorTimeOffRepo extends JpaRepository<DoctorTimeOff, Long> {

    // Get all time off for a doctor
    List<DoctorTimeOff> findByDoctorId(Long doctorId);

    // Check if doctor has time off on a specific date
    @Query("SELECT COUNT(t) > 0 FROM DoctorTimeOff t " +
           "WHERE t.doctor.id = :doctorId " +
           "AND :date BETWEEN t.startDate AND t.endDate")
    boolean isDoctorOffOnDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Get time off entries that overlap with a date range
    @Query("SELECT t FROM DoctorTimeOff t " +
           "WHERE t.doctor.id = :doctorId " +
           "AND t.startDate <= :endDate AND t.endDate >= :startDate")
    List<DoctorTimeOff> findOverlappingTimeOff(
        @Param("doctorId") Long doctorId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Get future time off for a doctor
    @Query("SELECT t FROM DoctorTimeOff t " +
           "WHERE t.doctor.id = :doctorId AND t.endDate >= :today " +
           "ORDER BY t.startDate")
    List<DoctorTimeOff> findFutureTimeOff(@Param("doctorId") Long doctorId, @Param("today") LocalDate today);
}
