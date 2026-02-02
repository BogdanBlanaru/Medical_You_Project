package com.zega.medical_you_be.model.entity.composite;

import java.io.Serializable;
import java.util.Objects;

public class DoctorPatientId implements Serializable {

    private Long patientId;
    private Long doctorId;

    // Default constructor
    public DoctorPatientId() {}

    public DoctorPatientId(Long patientId, Long doctorId) {
        this.patientId = patientId;
        this.doctorId = doctorId;
    }

    // Getters and Setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    // Equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoctorPatientId that = (DoctorPatientId) o;
        return Objects.equals(patientId, that.patientId) &&
                Objects.equals(doctorId, that.doctorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId, doctorId);
    }
}
