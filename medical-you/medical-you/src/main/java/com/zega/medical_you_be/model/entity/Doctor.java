package com.zega.medical_you_be.model.entity;

import com.zega.medical_you_be.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "doctors")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String specialization;

    @Column
    private String hospital;

    @Column(name = "hospital_address")
    private String hospitalAddress;

    @Column
    private Double rating;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column
    private String education;

    @Column(name = "office_hours")
    private String officeHours;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column
    @Enumerated(EnumType.STRING)
    private Role role = Role.DOCTOR;

    @Column(name = "created_at")
    @LastModifiedDate
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // Email Verification Fields
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    // Password Reset Fields
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DoctorPatient> patients = new HashSet<>();

    public void addPatient(Patient patient) {
        DoctorPatient doctorPatient = new DoctorPatient(patient, this);
        patients.add(doctorPatient);
        patient.getDoctors().add(doctorPatient);
    }

    // Remove method
    public void removePatient(Patient patient) {
        DoctorPatient doctorPatient = new DoctorPatient(patient, this);
        patients.remove(doctorPatient);
        patient.getDoctors().remove(doctorPatient);
    }
}
