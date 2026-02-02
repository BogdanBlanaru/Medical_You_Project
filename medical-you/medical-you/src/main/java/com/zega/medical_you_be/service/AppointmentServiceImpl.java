package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.AppointmentDto;
import com.zega.medical_you_be.model.entity.Appointment;
import com.zega.medical_you_be.model.entity.Doctor;
import com.zega.medical_you_be.model.entity.Patient;
import com.zega.medical_you_be.model.enums.AppointmentStatus;
import com.zega.medical_you_be.repo.AppointmentRepository;
import com.zega.medical_you_be.repo.DoctorRepo;
import com.zega.medical_you_be.repo.PatientRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    @Override
    @Transactional
    public AppointmentDto createAppointment(AppointmentDto appointmentDto) {
        Doctor doctor = doctorRepo.findById(appointmentDto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Patient patient = patientRepo.findById(appointmentDto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(appointmentDto.getAppointmentDate())
                .status(AppointmentStatus.SCHEDULED)
                .reason(appointmentDto.getReason())
                .notes(appointmentDto.getNotes())
                .isCancelled(false)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return mapToDto(saved);
    }

    @Override
    public AppointmentDto getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return mapToDto(appointment);
    }

    @Override
    public List<AppointmentDto> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndIsCancelledFalse(doctorId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDto> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findPatientAppointmentsOrderByDateDesc(patientId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDto> getDoctorAppointmentsInDateRange(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findDoctorAppointmentsInDateRange(doctorId, startDate, endDate)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentDto updateAppointment(Long id, AppointmentDto appointmentDto) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointmentDto.getAppointmentDate() != null) {
            appointment.setAppointmentDate(appointmentDto.getAppointmentDate());
        }
        if (appointmentDto.getStatus() != null) {
            appointment.setStatus(appointmentDto.getStatus());
        }
        if (appointmentDto.getReason() != null) {
            appointment.setReason(appointmentDto.getReason());
        }
        if (appointmentDto.getNotes() != null) {
            appointment.setNotes(appointmentDto.getNotes());
        }

        Appointment updated = appointmentRepository.save(appointment);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public boolean cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setIsCancelled(true);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return true;
    }

    @Override
    public List<AppointmentDto> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AppointmentDto mapToDto(Appointment appointment) {
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
}
