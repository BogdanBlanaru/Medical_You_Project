package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.AppointmentDto;
import com.zega.medical_you_be.model.dto.DoctorAvailabilityDto;
import com.zega.medical_you_be.model.dto.TimeSlotDto;
import com.zega.medical_you_be.model.entity.*;
import com.zega.medical_you_be.model.entity.composite.DoctorPatientId;
import com.zega.medical_you_be.model.enums.AppointmentStatus;
import com.zega.medical_you_be.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final DoctorAvailabilityRepo availabilityRepo;
    private final DoctorTimeOffRepo timeOffRepo;
    private final AppointmentRepository appointmentRepo;
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;
    private final DoctorPatientRepo doctorPatientRepo;
    private final EmailService emailService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    // ==================== AVAILABILITY MANAGEMENT ====================

    @Override
    public List<DoctorAvailabilityDto> getDoctorSchedule(Long doctorId) {
        return availabilityRepo.findDoctorSchedule(doctorId).stream()
                .map(this::mapToAvailabilityDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DoctorAvailabilityDto setDoctorAvailability(Long doctorId, DoctorAvailabilityDto dto) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorAvailability availability;
        if (dto.getId() != null) {
            availability = availabilityRepo.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Availability not found"));
        } else {
            availability = new DoctorAvailability();
            availability.setDoctor(doctor);
        }

        availability.setDayOfWeek(dto.getDayOfWeek());
        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());
        availability.setSlotDurationMinutes(dto.getSlotDurationMinutes() != null ? dto.getSlotDurationMinutes() : 30);
        availability.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        availability = availabilityRepo.save(availability);
        return mapToAvailabilityDto(availability);
    }

    @Override
    @Transactional
    public List<DoctorAvailabilityDto> setDoctorWeeklySchedule(Long doctorId, List<DoctorAvailabilityDto> schedule) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Deactivate existing schedule
        List<DoctorAvailability> existing = availabilityRepo.findByDoctorIdAndIsActiveTrue(doctorId);
        existing.forEach(a -> a.setIsActive(false));
        availabilityRepo.saveAll(existing);

        // Create new schedule
        List<DoctorAvailability> newSchedule = schedule.stream()
                .map(dto -> DoctorAvailability.builder()
                        .doctor(doctor)
                        .dayOfWeek(dto.getDayOfWeek())
                        .startTime(dto.getStartTime())
                        .endTime(dto.getEndTime())
                        .slotDurationMinutes(dto.getSlotDurationMinutes() != null ? dto.getSlotDurationMinutes() : 30)
                        .isActive(true)
                        .build())
                .collect(Collectors.toList());

        return availabilityRepo.saveAll(newSchedule).stream()
                .map(this::mapToAvailabilityDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAvailability(Long availabilityId) {
        availabilityRepo.deleteById(availabilityId);
    }

    // ==================== TIME SLOTS ====================

    @Override
    public List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
        List<TimeSlotDto> slots = new ArrayList<>();

        // Check if doctor is off on this date
        if (timeOffRepo.isDoctorOffOnDate(doctorId, date)) {
            return slots; // Empty - doctor is off
        }

        // Get availability for this day of week
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<DoctorAvailability> availabilities = availabilityRepo
                .findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);

        if (availabilities.isEmpty()) {
            return slots; // No availability configured for this day
        }

        // Get existing appointments for this date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> existingAppointments = appointmentRepo
                .findDoctorAppointmentsInDateRange(doctorId, startOfDay, endOfDay);

        Set<LocalTime> bookedTimes = existingAppointments.stream()
                .filter(a -> !a.getIsCancelled())
                .map(a -> a.getAppointmentDate().toLocalTime())
                .collect(Collectors.toSet());

        // Generate time slots
        for (DoctorAvailability availability : availabilities) {
            LocalTime current = availability.getStartTime();
            int slotDuration = availability.getSlotDurationMinutes();

            while (current.plusMinutes(slotDuration).isBefore(availability.getEndTime()) ||
                   current.plusMinutes(slotDuration).equals(availability.getEndTime())) {

                boolean isBooked = bookedTimes.contains(current);
                boolean isPast = date.equals(LocalDate.now()) && current.isBefore(LocalTime.now());

                slots.add(TimeSlotDto.builder()
                        .date(date)
                        .startTime(current)
                        .endTime(current.plusMinutes(slotDuration))
                        .dateTime(LocalDateTime.of(date, current))
                        .isAvailable(!isBooked && !isPast)
                        .displayTime(current.format(TIME_FORMATTER))
                        .build());

                current = current.plusMinutes(slotDuration);
            }
        }

        return slots.stream()
                .sorted(Comparator.comparing(TimeSlotDto::getStartTime))
                .collect(Collectors.toList());
    }

    @Override
    public Map<LocalDate, List<TimeSlotDto>> getAvailableSlotsForDateRange(
            Long doctorId, LocalDate startDate, LocalDate endDate) {

        Map<LocalDate, List<TimeSlotDto>> result = new LinkedHashMap<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<TimeSlotDto> slots = getAvailableSlots(doctorId, current);
            // Only include days that have at least one available slot
            if (slots.stream().anyMatch(TimeSlotDto::getIsAvailable)) {
                result.put(current, slots);
            }
            current = current.plusDays(1);
        }

        return result;
    }

    @Override
    public boolean isSlotAvailable(Long doctorId, LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        // Check time off
        if (timeOffRepo.isDoctorOffOnDate(doctorId, date)) {
            return false;
        }

        // Check availability configuration
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<DoctorAvailability> availabilities = availabilityRepo
                .findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);

        boolean withinSchedule = availabilities.stream()
                .anyMatch(a -> !time.isBefore(a.getStartTime()) && time.isBefore(a.getEndTime()));

        if (!withinSchedule) {
            return false;
        }

        // Check existing appointments
        LocalDateTime slotStart = dateTime;
        LocalDateTime slotEnd = dateTime.plusMinutes(30); // Default slot duration

        List<Appointment> conflicts = appointmentRepo.findDoctorAppointmentsInDateRange(doctorId, slotStart, slotEnd);
        return conflicts.stream().noneMatch(a -> !a.getIsCancelled());
    }

    // ==================== BOOKING OPERATIONS ====================

    @Override
    @Transactional
    public AppointmentDto bookAppointment(Long patientId, Long doctorId, LocalDateTime dateTime, String reason) {
        // Validate slot availability
        if (!isSlotAvailable(doctorId, dateTime)) {
            throw new RuntimeException("Selected time slot is not available");
        }

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(dateTime)
                .status(AppointmentStatus.SCHEDULED)
                .reason(reason)
                .isCancelled(false)
                .build();

        appointment = appointmentRepo.save(appointment);
        log.info("Appointment booked: {} for patient {} with doctor {} at {}",
                appointment.getId(), patientId, doctorId, dateTime);

        // Ensure DoctorPatient relationship exists
        DoctorPatientId dpId = new DoctorPatientId(patient.getId(), doctor.getId());
        if (!doctorPatientRepo.existsById(dpId)) {
            DoctorPatient doctorPatient = new DoctorPatient(patient, doctor);
            doctorPatientRepo.save(doctorPatient);
            log.info("Created DoctorPatient relationship between patient {} and doctor {}", patientId, doctorId);
        }

        // Send confirmation emails
        sendAppointmentConfirmationEmails(appointment);

        return mapToAppointmentDto(appointment);
    }

    @Override
    @Transactional
    public AppointmentDto rescheduleAppointment(Long appointmentId, LocalDateTime newDateTime) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getIsCancelled()) {
            throw new RuntimeException("Cannot reschedule a cancelled appointment");
        }

        // Check if new slot is available
        if (!isSlotAvailable(appointment.getDoctor().getId(), newDateTime)) {
            throw new RuntimeException("Selected time slot is not available");
        }

        LocalDateTime oldDateTime = appointment.getAppointmentDate();
        appointment.setAppointmentDate(newDateTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes((appointment.getNotes() != null ? appointment.getNotes() + "\n" : "") +
                "Rescheduled from " + oldDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        appointment = appointmentRepo.save(appointment);
        log.info("Appointment {} rescheduled from {} to {}", appointmentId, oldDateTime, newDateTime);

        // Send rescheduled email to patient
        sendAppointmentRescheduledEmail(appointment, oldDateTime);

        return mapToAppointmentDto(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, String cancellationReason) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setIsCancelled(true);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (cancellationReason != null && !cancellationReason.isEmpty()) {
            appointment.setNotes((appointment.getNotes() != null ? appointment.getNotes() + "\n" : "") +
                    "Cancellation reason: " + cancellationReason);
        }

        appointmentRepo.save(appointment);
        log.info("Appointment {} cancelled", appointmentId);

        // Send cancellation email to patient
        sendAppointmentCancelledEmail(appointment, cancellationReason);
    }

    @Override
    @Transactional
    public AppointmentDto confirmAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment = appointmentRepo.save(appointment);

        return mapToAppointmentDto(appointment);
    }

    // ==================== PATIENT APPOINTMENTS ====================

    @Override
    public List<AppointmentDto> getPatientUpcomingAppointments(Long patientId) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepo.findByPatientIdAndIsCancelledFalse(patientId).stream()
                .filter(a -> a.getAppointmentDate().isAfter(now))
                .sorted(Comparator.comparing(Appointment::getAppointmentDate))
                .map(this::mapToAppointmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDto> getPatientPastAppointments(Long patientId) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepo.findPatientAppointmentsOrderByDateDesc(patientId).stream()
                .filter(a -> a.getAppointmentDate().isBefore(now) || a.getIsCancelled())
                .map(this::mapToAppointmentDto)
                .collect(Collectors.toList());
    }

    // ==================== TIME OFF ====================

    @Override
    @Transactional
    public void addDoctorTimeOff(Long doctorId, LocalDate startDate, LocalDate endDate, String reason) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorTimeOff timeOff = DoctorTimeOff.builder()
                .doctor(doctor)
                .startDate(startDate)
                .endDate(endDate)
                .reason(reason)
                .build();

        timeOffRepo.save(timeOff);
        log.info("Time off added for doctor {} from {} to {}", doctorId, startDate, endDate);
    }

    @Override
    @Transactional
    public void removeDoctorTimeOff(Long timeOffId) {
        timeOffRepo.deleteById(timeOffId);
    }

    // ==================== HELPER METHODS ====================

    private DoctorAvailabilityDto mapToAvailabilityDto(DoctorAvailability entity) {
        return DoctorAvailabilityDto.builder()
                .id(entity.getId())
                .doctorId(entity.getDoctor().getId())
                .dayOfWeek(entity.getDayOfWeek())
                .dayName(DayOfWeek.of(entity.getDayOfWeek()).getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .slotDurationMinutes(entity.getSlotDurationMinutes())
                .isActive(entity.getIsActive())
                .build();
    }

    private AppointmentDto mapToAppointmentDto(Appointment appointment) {
        return AppointmentDto.builder()
                .id(appointment.getId())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getName())
                .doctorSpecialization(appointment.getDoctor().getSpecialization())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getName())
                .appointmentDate(appointment.getAppointmentDate())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .isCancelled(appointment.getIsCancelled())
                .build();
    }

    // ==================== EMAIL NOTIFICATION HELPERS ====================

    private void sendAppointmentConfirmationEmails(Appointment appointment) {
        try {
            Patient patient = appointment.getPatient();
            Doctor doctor = appointment.getDoctor();

            // Send confirmation to patient
            emailService.sendAppointmentConfirmationEmail(
                    patient.getEmail(),
                    patient.getName(),
                    doctor.getName(),
                    doctor.getSpecialization(),
                    appointment.getAppointmentDate(),
                    appointment.getReason()
            );

            // Send notification to doctor
            emailService.sendDoctorNewAppointmentEmail(
                    doctor.getEmail(),
                    doctor.getName(),
                    patient.getName(),
                    appointment.getAppointmentDate(),
                    appointment.getReason()
            );
        } catch (Exception e) {
            log.error("Failed to send appointment confirmation emails for appointment {}: {}",
                    appointment.getId(), e.getMessage());
            // Don't throw - email failure should not fail the booking
        }
    }

    private void sendAppointmentRescheduledEmail(Appointment appointment, LocalDateTime oldDateTime) {
        try {
            Patient patient = appointment.getPatient();
            Doctor doctor = appointment.getDoctor();

            emailService.sendAppointmentRescheduledEmail(
                    patient.getEmail(),
                    patient.getName(),
                    doctor.getName(),
                    oldDateTime,
                    appointment.getAppointmentDate()
            );
        } catch (Exception e) {
            log.error("Failed to send appointment rescheduled email for appointment {}: {}",
                    appointment.getId(), e.getMessage());
        }
    }

    private void sendAppointmentCancelledEmail(Appointment appointment, String cancellationReason) {
        try {
            Patient patient = appointment.getPatient();
            Doctor doctor = appointment.getDoctor();

            emailService.sendAppointmentCancelledEmail(
                    patient.getEmail(),
                    patient.getName(),
                    doctor.getName(),
                    appointment.getAppointmentDate(),
                    cancellationReason
            );
        } catch (Exception e) {
            log.error("Failed to send appointment cancelled email for appointment {}: {}",
                    appointment.getId(), e.getMessage());
        }
    }
}
