package com.zega.medical_you_be.controller;

import com.zega.medical_you_be.model.dto.PatientProfileDto;
import com.zega.medical_you_be.model.dto.UpdatePatientProfileDto;
import com.zega.medical_you_be.service.PatientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/patient/profile")
@RequiredArgsConstructor
@Slf4j
public class PatientProfileController {

    private final PatientProfileService profileService;

    @Value("${app.upload.dir:uploads/avatars}")
    private String uploadDir;

    /**
     * Get current user's profile
     */
    @GetMapping
    public ResponseEntity<PatientProfileDto> getMyProfile() {
        Long patientId = getCurrentPatientId();
        PatientProfileDto profile = profileService.getProfileByPatientId(patientId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Get profile by patient ID (for admin/doctor access)
     */
    @GetMapping("/{patientId}")
    public ResponseEntity<PatientProfileDto> getProfileById(@PathVariable Long patientId) {
        PatientProfileDto profile = profileService.getProfileByPatientId(patientId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Get profile by medical ID (for emergency access)
     */
    @GetMapping("/medical-id/{medicalId}")
    public ResponseEntity<PatientProfileDto> getProfileByMedicalId(@PathVariable String medicalId) {
        PatientProfileDto profile = profileService.getProfileByMedicalId(medicalId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user's profile
     */
    @PutMapping
    public ResponseEntity<PatientProfileDto> updateMyProfile(
            @Valid @RequestBody UpdatePatientProfileDto updateDto) {
        Long patientId = getCurrentPatientId();
        PatientProfileDto updatedProfile = profileService.updateProfile(patientId, updateDto);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Upload avatar
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        Long patientId = getCurrentPatientId();
        String avatarUrl = profileService.uploadAvatar(patientId, file);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Avatar uploaded successfully",
                "avatarUrl", avatarUrl
        ));
    }

    /**
     * Serve avatar image
     */
    @GetMapping("/avatar/{filename}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = "image/jpeg"; // Default
                if (filename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (filename.endsWith(".webp")) {
                    contentType = "image/webp";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error serving avatar: {}", filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete avatar
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<Map<String, String>> deleteAvatar() {
        Long patientId = getCurrentPatientId();
        profileService.deleteAvatar(patientId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Avatar deleted successfully"
        ));
    }

    /**
     * Get Medical ID Card data
     */
    @GetMapping("/medical-id-card")
    public ResponseEntity<PatientProfileService.MedicalIdCardDto> getMedicalIdCard() {
        Long patientId = getCurrentPatientId();
        PatientProfileService.MedicalIdCardDto cardData = profileService.getMedicalIdCard(patientId);
        return ResponseEntity.ok(cardData);
    }

    /**
     * Get Medical ID Card data by patient ID (for doctor access)
     */
    @GetMapping("/{patientId}/medical-id-card")
    public ResponseEntity<PatientProfileService.MedicalIdCardDto> getMedicalIdCardByPatientId(
            @PathVariable Long patientId) {
        PatientProfileService.MedicalIdCardDto cardData = profileService.getMedicalIdCard(patientId);
        return ResponseEntity.ok(cardData);
    }

    // Helper method to get current patient ID from security context
    private Long getCurrentPatientId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }

        // The principal should contain the user ID or we need to look it up by email
        Object principal = auth.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }

        // If principal is a string (email), we need to look up the patient ID
        // This depends on your security configuration
        String email = principal.toString();
        PatientProfileDto profile = profileService.getProfileByEmail(email);
        return profile.getPatientId();
    }
}
