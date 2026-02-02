package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.DoctorDto;
import com.zega.medical_you_be.model.dto.ForgotPasswordRequest;
import com.zega.medical_you_be.model.dto.PatientDto;
import com.zega.medical_you_be.model.dto.ResetPasswordRequest;
import com.zega.medical_you_be.model.dto.UserAuthRequest;
import com.zega.medical_you_be.model.entity.Doctor;
import com.zega.medical_you_be.model.entity.Patient;
import com.zega.medical_you_be.model.enums.Role;
import com.zega.medical_you_be.repo.DoctorRepo;
import com.zega.medical_you_be.repo.PatientRepo;
import com.zega.medical_you_be.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    private final EmailService emailService;

    @Value("${password.reset.token.expiration}")
    private long passwordResetExpiration;

    @Value("${email.verification.token.expiration}")
    private long emailVerificationExpiration;

    // ==================== REGISTRATION ====================

    @Override
    @Transactional
    public boolean savePatient(PatientDto patientDto) {
        if (patientDto == null) {
            return false;
        }

        // Check if email already exists
        if (patientRepo.findByEmail(patientDto.getEmail()).isPresent()) {
            LOGGER.warn("Email already exists: {}", patientDto.getEmail());
            return false;
        }

        // Generate verification token
        String verificationToken = generateToken();
        LocalDateTime verificationExpiry = LocalDateTime.now()
                .plusSeconds(emailVerificationExpiration / 1000);

        Patient patient = Patient.builder()
                .name(patientDto.getName())
                .email(patientDto.getEmail())
                .password(passwordEncoder.encode(patientDto.getPassword()))
                .role(patientDto.getRole())
                .isDeleted(false)
                .emailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(verificationExpiry)
                .build();

        patientRepo.save(patient);

        // Send verification email
        emailService.sendEmailVerificationEmail(
                patient.getEmail(),
                patient.getName(),
                verificationToken
        );

        LOGGER.info("Patient registered successfully: {}", patientDto.getEmail());
        return true;
    }

    @Override
    @Transactional
    public boolean saveDoctor(DoctorDto doctorDto) {
        if (doctorDto == null) {
            return false;
        }

        // Check if email already exists
        if (doctorRepo.findByEmail(doctorDto.getEmail()).isPresent()) {
            LOGGER.warn("Email already exists: {}", doctorDto.getEmail());
            return false;
        }

        // Generate verification token
        String verificationToken = generateToken();
        LocalDateTime verificationExpiry = LocalDateTime.now()
                .plusSeconds(emailVerificationExpiration / 1000);

        Doctor doctor = Doctor.builder()
                .name(doctorDto.getName())
                .email(doctorDto.getEmail())
                .password(passwordEncoder.encode(doctorDto.getPassword()))
                .specialization(doctorDto.getSpecialization())
                .hospital(doctorDto.getHospital())
                .hospitalAddress(doctorDto.getHospitalAddress())
                .rating(doctorDto.getRating())
                .yearsOfExperience(doctorDto.getYearsOfExperience())
                .education(doctorDto.getEducation())
                .officeHours(doctorDto.getOfficeHours())
                .contactNumber(doctorDto.getContactNumber())
                .role(doctorDto.getRole())
                .isDeleted(false)
                .emailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(verificationExpiry)
                .build();

        doctorRepo.save(doctor);

        // Send verification email
        emailService.sendEmailVerificationEmail(
                doctor.getEmail(),
                doctor.getName(),
                verificationToken
        );

        LOGGER.info("Doctor registered successfully: {}", doctorDto.getEmail());
        return true;
    }

    // ==================== AUTHENTICATION ====================

    @Override
    public String authenticate(UserAuthRequest request) {
        userDetailsService.setRole(request.getRole());

        if (request.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepo.findByEmail(request.getUsername()).orElse(null);
            if (doctor != null) {
                // Check if email is verified
                if (!doctor.getEmailVerified()) {
                    LOGGER.warn("Email not verified for doctor: {}", request.getUsername());
                    throw new RuntimeException("EMAIL_NOT_VERIFIED");
                }

                if (authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()))
                        .isAuthenticated()) {
                    // Generate token with remember me support
                    var token = jwtUtil.generateToken(request.getUsername(), request.isRememberMe());
                    tokenService.revokeAllTokens(doctor.getId());
                    tokenService.saveDoctorToken(doctor, token);
                    LOGGER.info("Doctor logged in successfully: {} (rememberMe: {})",
                            request.getUsername(), request.isRememberMe());
                    return token;
                }
            }
        } else if (request.getRole() == Role.PATIENT) {
            Patient patient = patientRepo.findByEmail(request.getUsername()).orElse(null);
            if (patient != null) {
                // Check if email is verified
                if (!patient.getEmailVerified()) {
                    LOGGER.warn("Email not verified for patient: {}", request.getUsername());
                    throw new RuntimeException("EMAIL_NOT_VERIFIED");
                }

                if (authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()))
                        .isAuthenticated()) {
                    // Generate token with remember me support
                    var token = jwtUtil.generateToken(request.getUsername(), request.isRememberMe());
                    tokenService.revokeAllTokens(patient.getId());
                    tokenService.savePatientToken(patient, token);
                    LOGGER.info("Patient logged in successfully: {} (rememberMe: {})",
                            request.getUsername(), request.isRememberMe());
                    return token;
                }
            }
        }
        return null;
    }

    // ==================== PASSWORD RESET ====================

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String resetToken = generateToken();
        LocalDateTime resetExpiry = LocalDateTime.now()
                .plusSeconds(passwordResetExpiration / 1000);

        if ("PATIENT".equalsIgnoreCase(request.getRole())) {
            Patient patient = patientRepo.findByEmail(request.getEmail()).orElse(null);
            if (patient != null) {
                patient.setResetToken(resetToken);
                patient.setResetTokenExpiry(resetExpiry);
                patientRepo.save(patient);

                emailService.sendPasswordResetEmail(
                        patient.getEmail(),
                        patient.getName(),
                        resetToken
                );
                LOGGER.info("Password reset email sent to patient: {}", request.getEmail());
            }
        } else if ("DOCTOR".equalsIgnoreCase(request.getRole())) {
            Doctor doctor = doctorRepo.findByEmail(request.getEmail()).orElse(null);
            if (doctor != null) {
                doctor.setResetToken(resetToken);
                doctor.setResetTokenExpiry(resetExpiry);
                doctorRepo.save(doctor);

                emailService.sendPasswordResetEmail(
                        doctor.getEmail(),
                        doctor.getName(),
                        resetToken
                );
                LOGGER.info("Password reset email sent to doctor: {}", request.getEmail());
            }
        }
        // Always return success to prevent email enumeration attacks
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Try to find patient with this token
        Patient patient = patientRepo.findByResetToken(request.getToken()).orElse(null);
        if (patient != null) {
            if (isTokenExpired(patient.getResetTokenExpiry())) {
                throw new RuntimeException("RESET_TOKEN_EXPIRED");
            }

            patient.setPassword(passwordEncoder.encode(request.getNewPassword()));
            patient.setResetToken(null);
            patient.setResetTokenExpiry(null);
            patientRepo.save(patient);

            emailService.sendPasswordChangedEmail(patient.getEmail(), patient.getName());
            LOGGER.info("Password reset successful for patient: {}", patient.getEmail());
            return;
        }

        // Try to find doctor with this token
        Doctor doctor = doctorRepo.findByResetToken(request.getToken()).orElse(null);
        if (doctor != null) {
            if (isTokenExpired(doctor.getResetTokenExpiry())) {
                throw new RuntimeException("RESET_TOKEN_EXPIRED");
            }

            doctor.setPassword(passwordEncoder.encode(request.getNewPassword()));
            doctor.setResetToken(null);
            doctor.setResetTokenExpiry(null);
            doctorRepo.save(doctor);

            emailService.sendPasswordChangedEmail(doctor.getEmail(), doctor.getName());
            LOGGER.info("Password reset successful for doctor: {}", doctor.getEmail());
            return;
        }

        throw new RuntimeException("INVALID_RESET_TOKEN");
    }

    @Override
    public boolean validateResetToken(String token) {
        // Check in patients
        Patient patient = patientRepo.findByResetToken(token).orElse(null);
        if (patient != null) {
            return !isTokenExpired(patient.getResetTokenExpiry());
        }

        // Check in doctors
        Doctor doctor = doctorRepo.findByResetToken(token).orElse(null);
        if (doctor != null) {
            return !isTokenExpired(doctor.getResetTokenExpiry());
        }

        return false;
    }

    // ==================== EMAIL VERIFICATION ====================

    @Override
    @Transactional
    public void verifyEmail(String token) {
        // Try to find patient with this token
        Patient patient = patientRepo.findByVerificationToken(token).orElse(null);
        if (patient != null) {
            if (isTokenExpired(patient.getVerificationTokenExpiry())) {
                throw new RuntimeException("VERIFICATION_TOKEN_EXPIRED");
            }

            patient.setEmailVerified(true);
            patient.setVerificationToken(null);
            patient.setVerificationTokenExpiry(null);
            patientRepo.save(patient);

            emailService.sendWelcomeEmail(patient.getEmail(), patient.getName());
            LOGGER.info("Email verified for patient: {}", patient.getEmail());
            return;
        }

        // Try to find doctor with this token
        Doctor doctor = doctorRepo.findByVerificationToken(token).orElse(null);
        if (doctor != null) {
            if (isTokenExpired(doctor.getVerificationTokenExpiry())) {
                throw new RuntimeException("VERIFICATION_TOKEN_EXPIRED");
            }

            doctor.setEmailVerified(true);
            doctor.setVerificationToken(null);
            doctor.setVerificationTokenExpiry(null);
            doctorRepo.save(doctor);

            emailService.sendWelcomeEmail(doctor.getEmail(), doctor.getName());
            LOGGER.info("Email verified for doctor: {}", doctor.getEmail());
            return;
        }

        throw new RuntimeException("INVALID_VERIFICATION_TOKEN");
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email, String role) {
        String verificationToken = generateToken();
        LocalDateTime verificationExpiry = LocalDateTime.now()
                .plusSeconds(emailVerificationExpiration / 1000);

        if ("PATIENT".equalsIgnoreCase(role)) {
            Patient patient = patientRepo.findByEmail(email).orElse(null);
            if (patient != null && !patient.getEmailVerified()) {
                patient.setVerificationToken(verificationToken);
                patient.setVerificationTokenExpiry(verificationExpiry);
                patientRepo.save(patient);

                emailService.sendEmailVerificationEmail(
                        patient.getEmail(),
                        patient.getName(),
                        verificationToken
                );
                LOGGER.info("Verification email resent to patient: {}", email);
            }
        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            Doctor doctor = doctorRepo.findByEmail(email).orElse(null);
            if (doctor != null && !doctor.getEmailVerified()) {
                doctor.setVerificationToken(verificationToken);
                doctor.setVerificationTokenExpiry(verificationExpiry);
                doctorRepo.save(doctor);

                emailService.sendEmailVerificationEmail(
                        doctor.getEmail(),
                        doctor.getName(),
                        verificationToken
                );
                LOGGER.info("Verification email resent to doctor: {}", email);
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private boolean isTokenExpired(LocalDateTime expiry) {
        return expiry == null || LocalDateTime.now().isAfter(expiry);
    }
}
