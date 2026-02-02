package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientProfileRepo extends JpaRepository<PatientProfile, Long> {

    Optional<PatientProfile> findByPatientId(Long patientId);

    Optional<PatientProfile> findByPatientEmail(String email);

    Optional<PatientProfile> findByMedicalId(String medicalId);

    boolean existsByPatientId(Long patientId);

    @Query("SELECT pp FROM PatientProfile pp JOIN FETCH pp.patient WHERE pp.patient.id = :patientId")
    Optional<PatientProfile> findByPatientIdWithPatient(@Param("patientId") Long patientId);
}
