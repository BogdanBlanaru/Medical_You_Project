package com.zega.medical_you_be.mapper;

import com.zega.medical_you_be.model.dto.DoctorDto;
import com.zega.medical_you_be.model.entity.Doctor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    Doctor toEntity(DoctorDto doctorDto);
}
