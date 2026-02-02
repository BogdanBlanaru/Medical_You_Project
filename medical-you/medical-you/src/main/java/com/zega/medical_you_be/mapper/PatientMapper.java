package com.zega.medical_you_be.mapper;

import com.zega.medical_you_be.model.dto.PatientDto;
import com.zega.medical_you_be.model.entity.Patient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    Patient toEntity(PatientDto patientDto);
}
