package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.PatientData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientDataRepo extends JpaRepository<PatientData, Long> {
}
