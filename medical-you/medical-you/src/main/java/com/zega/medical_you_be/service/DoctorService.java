package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.DoctorDto;

import java.util.List;
import java.util.Optional;

public interface DoctorService {

    List<DoctorDto> getAllDoctors();
    Optional<DoctorDto> getDoctorById(Long id);
}
