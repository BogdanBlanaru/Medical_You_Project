package com.zega.medical_you_be.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for resetting password with token.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    private String token;

    private String newPassword;
}
