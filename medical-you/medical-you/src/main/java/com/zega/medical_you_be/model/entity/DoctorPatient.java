package com.zega.medical_you_be.model.entity;

import com.zega.medical_you_be.model.entity.composite.DoctorPatientId;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_patient")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DoctorPatient {

    @EmbeddedId
    private DoctorPatientId id;

    @ManyToOne
    @MapsId("patientId")
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @MapsId("doctorId")
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "created_at")
    @LastModifiedDate
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    public DoctorPatient(Patient patient, Doctor doctor) {
        this.patient = patient;
        this.doctor = doctor;
        this.id = new DoctorPatientId(patient.getId(), doctor.getId());
    }
}
