package com.zega.medical_you_be.controller;

import com.zega.medical_you_be.model.dto.DoctorDto;
import com.zega.medical_you_be.model.dto.ForgotPasswordRequest;
import com.zega.medical_you_be.model.dto.PatientDto;
import com.zega.medical_you_be.model.dto.ResetPasswordRequest;
import com.zega.medical_you_be.model.dto.UserAuthRequest;
import com.zega.medical_you_be.service.AuthService;
import com.zega.medical_you_be.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final TokenService tokenService;

    // ==================== REGISTRATION ====================

    @PostMapping("/register/doctor")
    public ResponseEntity<Map<String, String>> registerDoctor(@RequestBody DoctorDto doctorDto) {
        LOGGER.info("Doctor registration request for: {}", doctorDto != null ? doctorDto.getEmail() : "NULL");

        Map<String, String> response = new HashMap<>();

        if (authService.saveDoctor(doctorDto)) {
            response.put("status", "success");
            response.put("message", "Registration successful! Please check your email to verify your account.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Registration failed. Email may already be in use.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/register/patient")
    public ResponseEntity<Map<String, String>> registerPatient(@RequestBody PatientDto patientDto) {
        LOGGER.info("Patient registration request for: {}", patientDto != null ? patientDto.getEmail() : "NULL");

        Map<String, String> response = new HashMap<>();

        if (authService.savePatient(patientDto)) {
            response.put("status", "success");
            response.put("message", "Registration successful! Please check your email to verify your account.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Registration failed. Email may already be in use.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== AUTHENTICATION ====================

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserAuthRequest userAuthRequest) {
        try {
            String token = authService.authenticate(userAuthRequest);
            if (token == null) {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Invalid email or password.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");

            if ("EMAIL_NOT_VERIFIED".equals(e.getMessage())) {
                error.put("code", "EMAIL_NOT_VERIFIED");
                error.put("message", "Please verify your email before logging in. Check your inbox for the verification link.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            error.put("message", "Authentication failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, String> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("status", "error");
            response.put("message", "Missing or malformed authorization header.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String token = authHeader.substring(7);

        if (tokenService.revokeToken(token)) {
            response.put("status", "success");
            response.put("message", "Successfully logged out.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid or expired token.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== PASSWORD RESET ====================

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        LOGGER.info("Password reset requested for: {}", request.getEmail());

        authService.requestPasswordReset(request);

        // Always return success to prevent email enumeration
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "If an account exists with this email, you will receive password reset instructions.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-reset-token/{token}")
    public ResponseEntity<Map<String, Object>> validateResetToken(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        boolean isValid = authService.validateResetToken(token);

        response.put("valid", isValid);
        if (!isValid) {
            response.put("message", "Invalid or expired reset token.");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        LOGGER.info("Password reset attempt with token");

        Map<String, String> response = new HashMap<>();

        try {
            authService.resetPassword(request);
            response.put("status", "success");
            response.put("message", "Password has been reset successfully. You can now log in with your new password.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("status", "error");

            if ("RESET_TOKEN_EXPIRED".equals(e.getMessage())) {
                response.put("code", "TOKEN_EXPIRED");
                response.put("message", "This password reset link has expired. Please request a new one.");
            } else if ("INVALID_RESET_TOKEN".equals(e.getMessage())) {
                response.put("code", "INVALID_TOKEN");
                response.put("message", "Invalid password reset link. Please request a new one.");
            } else {
                response.put("message", "Failed to reset password. Please try again.");
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== EMAIL VERIFICATION ====================

    @GetMapping("/verify-email/{token}")
    public ResponseEntity<Map<String, String>> verifyEmail(@PathVariable String token) {
        LOGGER.info("Email verification attempt with token");

        Map<String, String> response = new HashMap<>();

        try {
            authService.verifyEmail(token);
            response.put("status", "success");
            response.put("message", "Email verified successfully! You can now log in to your account.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("status", "error");

            if ("VERIFICATION_TOKEN_EXPIRED".equals(e.getMessage())) {
                response.put("code", "TOKEN_EXPIRED");
                response.put("message", "This verification link has expired. Please request a new one.");
            } else if ("INVALID_VERIFICATION_TOKEN".equals(e.getMessage())) {
                response.put("code", "INVALID_TOKEN");
                response.put("message", "Invalid verification link.");
            } else {
                response.put("message", "Failed to verify email. Please try again.");
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String role = request.get("role");

        LOGGER.info("Resend verification email requested for: {}", email);

        authService.resendVerificationEmail(email, role);

        // Always return success to prevent email enumeration
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "If an unverified account exists with this email, a new verification link has been sent.");
        return ResponseEntity.ok(response);
    }
}
