package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorIdAndIsCancelledFalse(Long doctorId);

    List<Appointment> findByPatientIdAndIsCancelledFalse(Long patientId);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate BETWEEN :startDate AND :endDate AND a.isCancelled = false")
    List<Appointment> findDoctorAppointmentsInDateRange(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.isCancelled = false ORDER BY a.appointmentDate DESC")
    List<Appointment> findPatientAppointmentsOrderByDateDesc(@Param("patientId") Long patientId);
}
