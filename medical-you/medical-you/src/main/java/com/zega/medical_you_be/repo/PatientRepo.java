package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.dto.PatientDto;
import com.zega.medical_you_be.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepo extends JpaRepository<Patient, Long> {

    @Query("select new com.zega.medical_you_be.model.dto.PatientDto(" +
            "p.id, p.name, p.email, p.password, p.role) from Patient p " +
            "where p.isDeleted = false")
    List<PatientDto> getAllPatients();

    @Query("select new com.zega.medical_you_be.model.dto.PatientDto(" +
            "p.id, p.name, p.email, p.password, p.role) from Patient p " +
            "where p.isDeleted = false and p.id = :id")
    Optional<PatientDto> getPatientById(@Param("id") Long id);

    Optional<Patient> findByEmail(String email);

    // Password Reset
    Optional<Patient> findByResetToken(String resetToken);

    // Email Verification
    Optional<Patient> findByVerificationToken(String verificationToken);
}
