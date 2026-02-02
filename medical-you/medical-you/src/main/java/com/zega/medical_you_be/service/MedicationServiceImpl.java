package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.CreateMedicationDto;
import com.zega.medical_you_be.model.dto.MedicationDto;
import com.zega.medical_you_be.model.entity.FamilyMember;
import com.zega.medical_you_be.model.entity.Medication;
import com.zega.medical_you_be.model.entity.MedicationLog;
import com.zega.medical_you_be.model.entity.MedicationReminder;
import com.zega.medical_you_be.model.entity.Patient;
import com.zega.medical_you_be.model.enums.MedicationFrequency;
import com.zega.medical_you_be.model.enums.MedicationLogStatus;
import com.zega.medical_you_be.model.enums.MedicationStatus;
import com.zega.medical_you_be.repo.FamilyMemberRepo;
import com.zega.medical_you_be.repo.MedicationLogRepo;
import com.zega.medical_you_be.repo.MedicationRepo;
import com.zega.medical_you_be.repo.PatientRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepo medicationRepo;
    private final MedicationLogRepo medicationLogRepo;
    private final PatientRepo patientRepo;
    private final FamilyMemberRepo familyMemberRepo;

    @Override
    @Transactional
    public MedicationDto createMedication(String userEmail, CreateMedicationDto dto) {
        Patient patient = getPatientByEmail(userEmail);

        FamilyMember familyMember = null;
        if (dto.getFamilyMemberId() != null) {
            familyMember = familyMemberRepo.findById(dto.getFamilyMemberId())
                    .orElseThrow(() -> new RuntimeException("Family member not found"));
            if (!familyMember.getFamilyGroup().getCreatedBy().getId().equals(patient.getId())) {
                throw new RuntimeException("Access denied to this family member");
            }
        }

        Medication medication = Medication.builder()
                .patient(patient)
                .familyMember(familyMember)
                .name(dto.getName())
                .dosage(dto.getDosage())
                .frequency(dto.getFrequency())
                .timesPerDay(dto.getTimesPerDay() != null ? dto.getTimesPerDay() : getDefaultTimesPerDay(dto.getFrequency()))
                .specificTimes(dto.getSpecificTimes())
                .instructions(dto.getInstructions())
                .prescribedBy(dto.getPrescribedBy())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(MedicationStatus.ACTIVE)
                .refillReminderDays(dto.getRefillReminderDays())
                .pillsRemaining(dto.getPillsRemaining())
                .pillsPerDose(dto.getPillsPerDose() != null ? dto.getPillsPerDose() : 1)
                .color(dto.getColor())
                .notes(dto.getNotes())
                .build();

        // Create reminders based on specific times
        if (dto.getSpecificTimes() != null && !dto.getSpecificTimes().isEmpty()) {
            for (String timeStr : dto.getSpecificTimes()) {
                MedicationReminder reminder = MedicationReminder.builder()
                        .medication(medication)
                        .reminderTime(LocalTime.parse(timeStr))
                        .isEnabled(true)
                        .build();
                medication.getReminders().add(reminder);
            }
        }

        medication = medicationRepo.save(medication);
        log.info("Medication created: id={}, name={}", medication.getId(), medication.getName());

        return mapToDto(medication);
    }

    @Override
    public MedicationDto getMedication(String userEmail, Long medicationId) {
        Patient patient = getPatientByEmail(userEmail);
        Medication medication = medicationRepo.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (!medication.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToDto(medication);
    }

    @Override
    @Transactional
    public MedicationDto updateMedication(String userEmail, Long medicationId, CreateMedicationDto dto) {
        Patient patient = getPatientByEmail(userEmail);
        Medication medication = medicationRepo.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (!medication.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        medication.setName(dto.getName());
        medication.setDosage(dto.getDosage());
        medication.setFrequency(dto.getFrequency());
        medication.setTimesPerDay(dto.getTimesPerDay() != null ? dto.getTimesPerDay() : getDefaultTimesPerDay(dto.getFrequency()));
        medication.setSpecificTimes(dto.getSpecificTimes());
        medication.setInstructions(dto.getInstructions());
        medication.setPrescribedBy(dto.getPrescribedBy());
        medication.setStartDate(dto.getStartDate());
        medication.setEndDate(dto.getEndDate());
        medication.setRefillReminderDays(dto.getRefillReminderDays());
        medication.setPillsRemaining(dto.getPillsRemaining());
        medication.setPillsPerDose(dto.getPillsPerDose());
        medication.setColor(dto.getColor());
        medication.setNotes(dto.getNotes());

        // Update reminders
        medication.getReminders().clear();
        if (dto.getSpecificTimes() != null && !dto.getSpecificTimes().isEmpty()) {
            for (String timeStr : dto.getSpecificTimes()) {
                MedicationReminder reminder = MedicationReminder.builder()
                        .medication(medication)
                        .reminderTime(LocalTime.parse(timeStr))
                        .isEnabled(true)
                        .build();
                medication.getReminders().add(reminder);
            }
        }

        medication = medicationRepo.save(medication);
        return mapToDto(medication);
    }

    @Override
    @Transactional
    public void deleteMedication(String userEmail, Long medicationId) {
        Patient patient = getPatientByEmail(userEmail);
        Medication medication = medicationRepo.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (!medication.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        medicationRepo.delete(medication);
        log.info("Medication deleted: id={}", medicationId);
    }

    @Override
    public Page<MedicationDto> getMedications(String userEmail, Long familyMemberId, boolean activeOnly, Pageable pageable) {
        Patient patient = getPatientByEmail(userEmail);

        List<Medication> medications;
        if (activeOnly) {
            if (familyMemberId != null) {
                medications = medicationRepo.findByPatientIdAndFamilyMemberIdAndStatus(
                        patient.getId(), familyMemberId, MedicationStatus.ACTIVE);
            } else {
                medications = medicationRepo.findByPatientIdAndStatus(patient.getId(), MedicationStatus.ACTIVE);
            }
        } else {
            medications = medicationRepo.findByPatientIdOrderByCreatedAtDesc(patient.getId(), pageable).getContent();
        }

        List<MedicationDto> dtos = medications.stream().map(this::mapToDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, dtos.size());
    }

    @Override
    public List<MedicationDto> getTodaySchedule(String userEmail, Long familyMemberId) {
        Patient patient = getPatientByEmail(userEmail);
        List<Medication> medications = medicationRepo.findActiveForToday(patient.getId(), LocalDate.now());

        if (familyMemberId != null) {
            medications = medications.stream()
                    .filter(m -> m.getFamilyMember() != null && m.getFamilyMember().getId().equals(familyMemberId))
                    .collect(Collectors.toList());
        }

        return medications.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<MedicationDto> getMedicationsNeedingRefill(String userEmail) {
        Patient patient = getPatientByEmail(userEmail);
        List<Medication> medications = medicationRepo.findNeedingRefill(patient.getId());
        return medications.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicationDto.LogDto logMedicationTaken(String userEmail, Long medicationId, String notes) {
        Patient patient = getPatientByEmail(userEmail);
        Medication medication = medicationRepo.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (!medication.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        MedicationLog log = MedicationLog.builder()
                .medication(medication)
                .takenAt(LocalDateTime.now())
                .status(MedicationLogStatus.TAKEN)
                .notes(notes)
                .build();

        log = medicationLogRepo.save(log);

        // Decrease pills count
        medication.decreasePills();
        medicationRepo.save(medication);

        return mapLogToDto(log);
    }

    @Override
    @Transactional
    public MedicationDto.LogDto logMedicationSkipped(String userEmail, Long medicationId, String notes) {
        Patient patient = getPatientByEmail(userEmail);
        Medication medication = medicationRepo.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (!medication.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        MedicationLog log = MedicationLog.builder()
                .medication(medication)
                .status(MedicationLogStatus.SKIPPED)
                .notes(notes)
                .build();

        log = medicationLogRepo.save(log);
        return mapLogToDto(log);
    }

    @Override
    public List<MedicationDto.LogDto> getMedicationLogs(String userEmail, Long medicationId, int days) {
        Patient patient = getPatientByEmail(userEmail);
        Medication medication = medicationRepo.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (!medication.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<MedicationLog> logs = medicationLogRepo.findByMedicationAndDateRange(
                medicationId, since, LocalDateTime.now());

        return logs.stream().map(this::mapLogToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicationDto pauseMedication(String userEmail, Long medicationId) {
        return updateMedicationStatus(userEmail, medicationId, MedicationStatus.PAUSED);
    }

    @Override
    @Transactional
    public MedicationDto resumeMedication(String userEmail, Long medicationId) {
        return updateMedicationStatus(userEmail, medicationId, MedicationStatus.ACTIVE);
    }

    @Override
    @Transactional
    public MedicationDto completeMedication(String userEmail, Long medicationId) {
        return updateMedicationStatus(userEmail, medicationId, MedicationStatus.COMPLETED);
    }

    private MedicationDto updateMedicationStatus(String userEmail, Long medicationId, MedicationStatus newStatus) {
        Patient patient = getPatientByEmail(userEmail);
        Medication medication = medicationRepo.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (!medication.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        medication.setStatus(newStatus);
        medication = medicationRepo.save(medication);
        return mapToDto(medication);
    }

    @Override
    public double getAdherenceRate(String userEmail, Long medicationId, int days) {
        Patient patient = getPatientByEmail(userEmail);
        Medication medication = medicationRepo.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (!medication.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Access denied");
        }

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        long takenCount = medicationLogRepo.countByMedicationAndStatusSince(medicationId, MedicationLogStatus.TAKEN, since);
        long totalCount = medicationLogRepo.countByMedicationSince(medicationId, since);

        if (totalCount == 0) return 100.0;
        return (takenCount * 100.0) / totalCount;
    }

    @Override
    public MedicationDashboard getDashboard(String userEmail, Long familyMemberId) {
        Patient patient = getPatientByEmail(userEmail);

        List<Medication> activeMeds = medicationRepo.findByPatientIdAndStatus(patient.getId(), MedicationStatus.ACTIVE);
        List<Medication> todayMeds = medicationRepo.findActiveForToday(patient.getId(), LocalDate.now());
        List<Medication> needRefill = medicationRepo.findNeedingRefill(patient.getId());

        // Filter by family member if specified
        if (familyMemberId != null) {
            activeMeds = activeMeds.stream()
                    .filter(m -> m.getFamilyMember() != null && m.getFamilyMember().getId().equals(familyMemberId))
                    .collect(Collectors.toList());
            todayMeds = todayMeds.stream()
                    .filter(m -> m.getFamilyMember() != null && m.getFamilyMember().getId().equals(familyMemberId))
                    .collect(Collectors.toList());
        }

        // Count today's logs
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<MedicationLog> todayLogs = medicationLogRepo.findTodayLogsForPatient(patient.getId(), startOfDay);
        int takenToday = (int) todayLogs.stream()
                .filter(l -> l.getStatus() == MedicationLogStatus.TAKEN)
                .count();

        // Calculate expected doses today
        int expectedToday = todayMeds.stream()
                .mapToInt(Medication::getTimesPerDay)
                .sum();

        // Calculate overall adherence (last 30 days)
        double totalAdherence = 0;
        int medsWithLogs = 0;
        for (Medication med : activeMeds) {
            LocalDateTime since = LocalDateTime.now().minusDays(30);
            long taken = medicationLogRepo.countByMedicationAndStatusSince(med.getId(), MedicationLogStatus.TAKEN, since);
            long total = medicationLogRepo.countByMedicationSince(med.getId(), since);
            if (total > 0) {
                totalAdherence += (taken * 100.0) / total;
                medsWithLogs++;
            }
        }
        double overallAdherence = medsWithLogs > 0 ? totalAdherence / medsWithLogs : 100.0;

        return new MedicationDashboard(
                activeMeds.size(),
                takenToday,
                Math.max(0, expectedToday - takenToday),
                needRefill.size(),
                Math.round(overallAdherence * 10) / 10.0,
                todayMeds.stream().map(this::mapToDto).collect(Collectors.toList()),
                needRefill.stream().map(this::mapToDto).collect(Collectors.toList())
        );
    }

    // ==================== Helper Methods ====================

    private Patient getPatientByEmail(String email) {
        return patientRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    private int getDefaultTimesPerDay(MedicationFrequency frequency) {
        return switch (frequency) {
            case ONCE_DAILY, EVERY_OTHER_DAY, WEEKLY -> 1;
            case TWICE_DAILY -> 2;
            case THREE_TIMES_DAILY -> 3;
            case FOUR_TIMES_DAILY -> 4;
            case AS_NEEDED, CUSTOM -> 1;
        };
    }

    private MedicationDto mapToDto(Medication medication) {
        List<MedicationDto.ReminderDto> reminders = medication.getReminders().stream()
                .map(r -> MedicationDto.ReminderDto.builder()
                        .id(r.getId())
                        .reminderTime(r.getReminderTime())
                        .isEnabled(r.getIsEnabled())
                        .label(r.getLabel())
                        .build())
                .collect(Collectors.toList());

        List<MedicationLog> recentLogs = medicationLogRepo.findTop10ByMedicationIdOrderByCreatedAtDesc(medication.getId());
        List<MedicationDto.LogDto> logDtos = recentLogs.stream()
                .map(this::mapLogToDto)
                .collect(Collectors.toList());

        // Calculate adherence rate (last 30 days)
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        long takenCount = medicationLogRepo.countByMedicationAndStatusSince(medication.getId(), MedicationLogStatus.TAKEN, since);
        long totalCount = medicationLogRepo.countByMedicationSince(medication.getId(), since);
        double adherenceRate = totalCount > 0 ? (takenCount * 100.0) / totalCount : 100.0;

        // Calculate days remaining
        int daysRemaining = 0;
        if (medication.getEndDate() != null) {
            daysRemaining = (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), medication.getEndDate());
        }

        return MedicationDto.builder()
                .id(medication.getId())
                .patientId(medication.getPatient().getId())
                .familyMemberId(medication.getFamilyMember() != null ? medication.getFamilyMember().getId() : null)
                .familyMemberName(medication.getFamilyMember() != null ? medication.getFamilyMember().getName() : null)
                .name(medication.getName())
                .dosage(medication.getDosage())
                .frequency(medication.getFrequency())
                .timesPerDay(medication.getTimesPerDay())
                .specificTimes(medication.getSpecificTimes())
                .instructions(medication.getInstructions())
                .prescribedBy(medication.getPrescribedBy())
                .startDate(medication.getStartDate())
                .endDate(medication.getEndDate())
                .status(medication.getStatus())
                .refillReminderDays(medication.getRefillReminderDays())
                .pillsRemaining(medication.getPillsRemaining())
                .pillsPerDose(medication.getPillsPerDose())
                .color(medication.getColor())
                .notes(medication.getNotes())
                .createdAt(medication.getCreatedAt())
                .updatedAt(medication.getUpdatedAt())
                .currentlyActive(medication.isCurrentlyActive())
                .needsRefill(medication.needsRefill())
                .daysRemaining(daysRemaining)
                .adherenceRate(Math.round(adherenceRate * 10) / 10.0)
                .reminders(reminders)
                .recentLogs(logDtos)
                .build();
    }

    private MedicationDto.LogDto mapLogToDto(MedicationLog log) {
        return MedicationDto.LogDto.builder()
                .id(log.getId())
                .scheduledTime(log.getScheduledTime())
                .takenAt(log.getTakenAt())
                .status(log.getStatus())
                .notes(log.getNotes())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
