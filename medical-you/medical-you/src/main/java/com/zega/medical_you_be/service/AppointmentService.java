package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.AppointmentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {

    AppointmentDto createAppointment(AppointmentDto appointmentDto);

    AppointmentDto getAppointmentById(Long id);

    List<AppointmentDto> getAppointmentsByDoctorId(Long doctorId);

    List<AppointmentDto> getAppointmentsByPatientId(Long patientId);

    List<AppointmentDto> getDoctorAppointmentsInDateRange(Long doctorId, LocalDateTime startDate, LocalDateTime endDate);

    AppointmentDto updateAppointment(Long id, AppointmentDto appointmentDto);

    boolean cancelAppointment(Long id);

    List<AppointmentDto> getAllAppointments();
}
