package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorAvailabilityRepo extends JpaRepository<DoctorAvailability, Long> {

    // Get all availability slots for a doctor
    List<DoctorAvailability> findByDoctorIdAndIsActiveTrue(Long doctorId);

    // Get availability for a specific day of week
    List<DoctorAvailability> findByDoctorIdAndDayOfWeekAndIsActiveTrue(Long doctorId, Integer dayOfWeek);

    // Get all active availability for a doctor ordered by day and time
    @Query("SELECT da FROM DoctorAvailability da " +
           "WHERE da.doctor.id = :doctorId AND da.isActive = true " +
           "ORDER BY da.dayOfWeek, da.startTime")
    List<DoctorAvailability> findDoctorSchedule(@Param("doctorId") Long doctorId);

    // Check if doctor has any availability configured
    boolean existsByDoctorIdAndIsActiveTrue(Long doctorId);

    // Delete all availability for a doctor
    void deleteByDoctorId(Long doctorId);
}
