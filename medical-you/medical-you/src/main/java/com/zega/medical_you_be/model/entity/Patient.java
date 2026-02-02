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
@Table(name = "patients")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.PATIENT;

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

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DoctorPatient> doctors = new HashSet<>();

    // Add method to assign a doctor
    public void addDoctor(Doctor doctor) {
        DoctorPatient doctorPatient = new DoctorPatient(this, doctor);
        doctors.add(doctorPatient);
        doctor.getPatients().add(doctorPatient);
    }

    // Remove method
    public void removeDoctor(Doctor doctor) {
        DoctorPatient doctorPatient = new DoctorPatient(this, doctor);
        doctors.remove(doctorPatient);
        doctor.getPatients().remove(doctorPatient);
    }
}
