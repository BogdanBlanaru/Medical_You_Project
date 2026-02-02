package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.PatientDto;

import java.util.List;
import java.util.Optional;

public interface PatientService {
    List<PatientDto> getAllPatients();
    Optional<PatientDto> getPatientById(Long id);
}
