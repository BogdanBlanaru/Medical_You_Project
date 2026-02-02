package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.DoctorDto;
import com.zega.medical_you_be.model.dto.ForgotPasswordRequest;
import com.zega.medical_you_be.model.dto.PatientDto;
import com.zega.medical_you_be.model.dto.ResetPasswordRequest;
import com.zega.medical_you_be.model.dto.UserAuthRequest;

public interface AuthService {

    // Registration
    boolean savePatient(PatientDto patientDto);
    boolean saveDoctor(DoctorDto doctorDto);

    // Authentication
    String authenticate(UserAuthRequest request);

    // Password Reset
    void requestPasswordReset(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    boolean validateResetToken(String token);

    // Email Verification
    void verifyEmail(String token);
    void resendVerificationEmail(String email, String role);
}
