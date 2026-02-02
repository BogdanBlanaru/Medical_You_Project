package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.AppointmentDto;
import com.zega.medical_you_be.model.dto.DoctorAvailabilityDto;
import com.zega.medical_you_be.model.dto.TimeSlotDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BookingService {

    // ==================== AVAILABILITY MANAGEMENT ====================

    /**
     * Get doctor's weekly schedule (availability configuration)
     */
    List<DoctorAvailabilityDto> getDoctorSchedule(Long doctorId);

    /**
     * Set or update doctor's availability for a day
     */
    DoctorAvailabilityDto setDoctorAvailability(Long doctorId, DoctorAvailabilityDto availability);

    /**
     * Set doctor's full weekly schedule
     */
    List<DoctorAvailabilityDto> setDoctorWeeklySchedule(Long doctorId, List<DoctorAvailabilityDto> schedule);

    /**
     * Delete a specific availability slot
     */
    void deleteAvailability(Long availabilityId);

    // ==================== TIME SLOTS ====================

    /**
     * Get available time slots for a doctor on a specific date
     */
    List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date);

    /**
     * Get available slots for a date range (for calendar view)
     * Returns a map of date -> list of available slots
     */
    Map<LocalDate, List<TimeSlotDto>> getAvailableSlotsForDateRange(
        Long doctorId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Check if a specific time slot is available
     */
    boolean isSlotAvailable(Long doctorId, LocalDateTime dateTime);

    // ==================== BOOKING OPERATIONS ====================

    /**
     * Book an appointment with conflict checking
     */
    AppointmentDto bookAppointment(Long patientId, Long doctorId, LocalDateTime dateTime, String reason);

    /**
     * Reschedule an existing appointment
     */
    AppointmentDto rescheduleAppointment(Long appointmentId, LocalDateTime newDateTime);

    /**
     * Cancel an appointment
     */
    void cancelAppointment(Long appointmentId, String cancellationReason);

    /**
     * Confirm an appointment (by doctor)
     */
    AppointmentDto confirmAppointment(Long appointmentId);

    // ==================== PATIENT APPOINTMENTS ====================

    /**
     * Get patient's upcoming appointments
     */
    List<AppointmentDto> getPatientUpcomingAppointments(Long patientId);

    /**
     * Get patient's past appointments
     */
    List<AppointmentDto> getPatientPastAppointments(Long patientId);

    // ==================== TIME OFF ====================

    /**
     * Add time off for a doctor
     */
    void addDoctorTimeOff(Long doctorId, LocalDate startDate, LocalDate endDate, String reason);

    /**
     * Remove time off
     */
    void removeDoctorTimeOff(Long timeOffId);
}
