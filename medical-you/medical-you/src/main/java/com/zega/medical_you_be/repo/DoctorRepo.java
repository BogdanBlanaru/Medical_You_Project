package com.zega.medical_you_be.repo;

import com.zega.medical_you_be.model.dto.DoctorDto;
import com.zega.medical_you_be.model.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor, Long> {

    @Query("select new com.zega.medical_you_be.model.dto.DoctorDto(" +
            "d.id, d.name, d.email, d.password, d.specialization, d.hospital, d.hospitalAddress, " +
            "d.rating, d.yearsOfExperience, d.education, d.officeHours, d.contactNumber, " +
            "d.role) from Doctor d " +
            "where d.isDeleted = false")
    List<DoctorDto> getAllDoctors();

    @Query("select new com.zega.medical_you_be.model.dto.DoctorDto(" +
            "d.id, d.name, d.email, d.password, d.specialization, d.hospital, d.hospitalAddress, " +
            "d.rating, d.yearsOfExperience, d.education, d.officeHours, d.contactNumber, " +
            "d.role) from Doctor d " +
            "where d.isDeleted = false and d.id = :id")
    Optional<DoctorDto> getDoctorById(@Param("id") Long id);

    Optional<Doctor> findByEmail(String email);

    // Password Reset
    Optional<Doctor> findByResetToken(String resetToken);

    // Email Verification
    Optional<Doctor> findByVerificationToken(String verificationToken);
}
