package com.zega.medical_you_be.service;


import com.zega.medical_you_be.model.dto.PatientDto;
import com.zega.medical_you_be.repo.PatientRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepo patientRepo;

    @Override
    public List<PatientDto> getAllPatients() {
        return patientRepo.getAllPatients();
    }

    @Override
    public Optional<PatientDto> getPatientById(Long id) {
        return patientRepo.getPatientById(id);
    }
}
