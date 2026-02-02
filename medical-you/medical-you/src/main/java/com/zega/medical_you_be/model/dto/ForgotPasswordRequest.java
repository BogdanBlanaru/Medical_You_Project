package com.zega.medical_you_be.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for password reset request (forgot password).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordRequest {

    private String email;

    private String role; // "PATIENT" or "DOCTOR"
}
