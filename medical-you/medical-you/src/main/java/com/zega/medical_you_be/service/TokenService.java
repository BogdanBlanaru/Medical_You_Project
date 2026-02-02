package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.entity.Doctor;
import com.zega.medical_you_be.model.entity.Patient;
import com.zega.medical_you_be.model.entity.Token;

public interface TokenService {

    Token saveDoctorToken(Doctor doctor, String token);

    Token savePatientToken(Patient patient, String token);

    boolean revokeToken(String token);

    void revokeAllTokens(Long id);
}
