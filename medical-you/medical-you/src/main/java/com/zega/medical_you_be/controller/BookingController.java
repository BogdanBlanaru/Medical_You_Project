package com.zega.medical_you_be.controller;

import com.zega.medical_you_be.model.dto.AppointmentDto;
import com.zega.medical_you_be.model.dto.DoctorAvailabilityDto;
import com.zega.medical_you_be.model.dto.TimeSlotDto;
import com.zega.medical_you_be.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    // ==================== AVAILABILITY ENDPOINTS ====================

    /**
     * Get doctor's weekly schedule configuration
     */
    @GetMapping("/doctor/{doctorId}/schedule")
    public ResponseEntity<List<DoctorAvailabilityDto>> getDoctorSchedule(@PathVariable Long doctorId) {
        return ResponseEntity.ok(bookingService.getDoctorSchedule(doctorId));
    }

    /**
     * Set doctor availability for a specific day
     */
    @PostMapping("/doctor/{doctorId}/availability")
    public ResponseEntity<DoctorAvailabilityDto> setDoctorAvailability(
            @PathVariable Long doctorId,
            @RequestBody DoctorAvailabilityDto availability) {
        return ResponseEntity.ok(bookingService.setDoctorAvailability(doctorId, availability));
    }

    /**
     * Set doctor's full weekly schedule
     */
    @PostMapping("/doctor/{doctorId}/weekly-schedule")
    public ResponseEntity<List<DoctorAvailabilityDto>> setDoctorWeeklySchedule(
            @PathVariable Long doctorId,
            @RequestBody List<DoctorAvailabilityDto> schedule) {
        return ResponseEntity.ok(bookingService.setDoctorWeeklySchedule(doctorId, schedule));
    }

    /**
     * Delete an availability slot
     */
    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<Map<String, String>> deleteAvailability(@PathVariable Long availabilityId) {
        bookingService.deleteAvailability(availabilityId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Availability deleted"));
    }

    // ==================== TIME SLOTS ENDPOINTS ====================

    /**
     * Get available time slots for a doctor on a specific date
     */
    @GetMapping("/doctor/{doctorId}/slots")
    public ResponseEntity<List<TimeSlotDto>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bookingService.getAvailableSlots(doctorId, date));
    }

    /**
     * Get available slots for a date range (calendar view)
     */
    @GetMapping("/doctor/{doctorId}/slots/range")
    public ResponseEntity<Map<LocalDate, List<TimeSlotDto>>> getAvailableSlotsForRange(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(bookingService.getAvailableSlotsForDateRange(doctorId, startDate, endDate));
    }

    /**
     * Check if a specific slot is available
     */
    @GetMapping("/doctor/{doctorId}/slot-available")
    public ResponseEntity<Map<String, Boolean>> checkSlotAvailability(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        boolean available = bookingService.isSlotAvailable(doctorId, dateTime);
        return ResponseEntity.ok(Map.of("available", available));
    }

    // ==================== BOOKING ENDPOINTS ====================

    /**
     * Book an appointment
     */
    @PostMapping("/book")
    public ResponseEntity<AppointmentDto> bookAppointment(@RequestBody BookAppointmentRequest request) {
        AppointmentDto appointment = bookingService.bookAppointment(
                request.patientId(),
                request.doctorId(),
                request.dateTime(),
                request.reason()
        );
        return ResponseEntity.ok(appointment);
    }

    /**
     * Reschedule an appointment
     */
    @PutMapping("/appointment/{appointmentId}/reschedule")
    public ResponseEntity<AppointmentDto> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @RequestBody RescheduleRequest request) {
        AppointmentDto appointment = bookingService.rescheduleAppointment(appointmentId, request.newDateTime());
        return ResponseEntity.ok(appointment);
    }

    /**
     * Cancel an appointment
     */
    @DeleteMapping("/appointment/{appointmentId}/cancel")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestParam(required = false) String reason) {
        bookingService.cancelAppointment(appointmentId, reason);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Appointment cancelled"));
    }

    /**
     * Confirm an appointment (doctor action)
     */
    @PutMapping("/appointment/{appointmentId}/confirm")
    public ResponseEntity<AppointmentDto> confirmAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(bookingService.confirmAppointment(appointmentId));
    }

    // ==================== PATIENT APPOINTMENTS ====================

    /**
     * Get patient's upcoming appointments
     */
    @GetMapping("/patient/{patientId}/upcoming")
    public ResponseEntity<List<AppointmentDto>> getPatientUpcomingAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(bookingService.getPatientUpcomingAppointments(patientId));
    }

    /**
     * Get patient's past appointments
     */
    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<List<AppointmentDto>> getPatientPastAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(bookingService.getPatientPastAppointments(patientId));
    }

    // ==================== TIME OFF ====================

    /**
     * Add time off for a doctor
     */
    @PostMapping("/doctor/{doctorId}/time-off")
    public ResponseEntity<Map<String, String>> addDoctorTimeOff(
            @PathVariable Long doctorId,
            @RequestBody TimeOffRequest request) {
        bookingService.addDoctorTimeOff(doctorId, request.startDate(), request.endDate(), request.reason());
        return ResponseEntity.ok(Map.of("status", "success", "message", "Time off added"));
    }

    /**
     * Remove time off
     */
    @DeleteMapping("/time-off/{timeOffId}")
    public ResponseEntity<Map<String, String>> removeDoctorTimeOff(@PathVariable Long timeOffId) {
        bookingService.removeDoctorTimeOff(timeOffId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Time off removed"));
    }

    // ==================== REQUEST RECORDS ====================

    record BookAppointmentRequest(
            Long patientId,
            Long doctorId,
            LocalDateTime dateTime,
            String reason
    ) {}

    record RescheduleRequest(LocalDateTime newDateTime) {}

    record TimeOffRequest(
            LocalDate startDate,
            LocalDate endDate,
            String reason
    ) {}
}
