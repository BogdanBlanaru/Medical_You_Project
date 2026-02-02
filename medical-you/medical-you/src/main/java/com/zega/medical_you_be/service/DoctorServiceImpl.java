package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.DoctorDto;
import com.zega.medical_you_be.repo.DoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepo doctorRepo;

    @Override
    public List<DoctorDto> getAllDoctors() {
        return doctorRepo.getAllDoctors();
    }

    @Override
    public Optional<DoctorDto> getDoctorById(Long id) {
        return doctorRepo.getDoctorById(id);
    }
}
