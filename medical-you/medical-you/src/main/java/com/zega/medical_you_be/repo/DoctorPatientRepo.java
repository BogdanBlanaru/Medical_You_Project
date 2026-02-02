package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.DoctorPatient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorPatientRepo extends JpaRepository<DoctorPatient, Long> {
}
