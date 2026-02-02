package com.zega.medical_you_be.controller;

import com.zega.medical_you_be.model.dto.*;
import com.zega.medical_you_be.model.entity.Patient;
import com.zega.medical_you_be.repo.PatientRepo;
import com.zega.medical_you_be.service.FamilyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
@Slf4j
public class FamilyController {

    private final FamilyService familyService;
    private final PatientRepo patientRepo;

    // ==================== Family Group Endpoints ====================

    /**
     * Get current user's family group
     */
    @GetMapping
    public ResponseEntity<FamilyGroupDto> getMyFamilyGroup() {
        Long patientId = getCurrentPatientId();
        FamilyGroupDto group = familyService.getMyFamilyGroup(patientId);

        if (group == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(group);
    }

    /**
     * Create a new family group
     */
    @PostMapping
    public ResponseEntity<FamilyGroupDto> createFamilyGroup(
            @RequestBody(required = false) Map<String, String> request) {
        Long patientId = getCurrentPatientId();
        String name = request != null ? request.get("name") : null;

        FamilyGroupDto group = familyService.createFamilyGroup(patientId, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    /**
     * Update family group name
     */
    @PutMapping
    public ResponseEntity<FamilyGroupDto> updateFamilyGroup(
            @RequestBody Map<String, String> request) {
        Long patientId = getCurrentPatientId();
        FamilyGroupDto existingGroup = familyService.getMyFamilyGroup(patientId);

        if (existingGroup == null) {
            return ResponseEntity.notFound().build();
        }

        // For now, just return the existing group
        // Name update can be added if needed
        return ResponseEntity.ok(existingGroup);
    }

    // ==================== Family Member Endpoints ====================

    /**
     * Get all family members
     */
    @GetMapping("/members")
    public ResponseEntity<List<FamilyMemberDto>> getFamilyMembers() {
        Long patientId = getCurrentPatientId();
        List<FamilyMemberDto> members = familyService.getFamilyMembers(patientId);
        return ResponseEntity.ok(members);
    }

    /**
     * Add a new family member
     */
    @PostMapping("/members")
    public ResponseEntity<FamilyMemberDto> addFamilyMember(
            @Valid @RequestBody AddFamilyMemberRequest request) {
        Long patientId = getCurrentPatientId();
        FamilyMemberDto member = familyService.addFamilyMember(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    /**
     * Get a specific family member
     */
    @GetMapping("/members/{memberId}")
    public ResponseEntity<FamilyMemberDto> getFamilyMember(@PathVariable Long memberId) {
        Long patientId = getCurrentPatientId();
        FamilyMemberDto member = familyService.getFamilyMemberById(memberId, patientId);
        return ResponseEntity.ok(member);
    }

    /**
     * Update a family member
     */
    @PutMapping("/members/{memberId}")
    public ResponseEntity<FamilyMemberDto> updateFamilyMember(
            @PathVariable Long memberId,
            @Valid @RequestBody AddFamilyMemberRequest request) {
        Long patientId = getCurrentPatientId();
        FamilyMemberDto member = familyService.updateFamilyMember(memberId, request, patientId);
        return ResponseEntity.ok(member);
    }

    /**
     * Remove a family member (soft delete)
     */
    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Map<String, String>> removeFamilyMember(@PathVariable Long memberId) {
        Long patientId = getCurrentPatientId();
        familyService.removeFamilyMember(memberId, patientId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Family member removed successfully"
        ));
    }

    // ==================== Family Member Profile Endpoints ====================

    /**
     * Get a family member's medical profile
     */
    @GetMapping("/members/{memberId}/profile")
    public ResponseEntity<DependentProfileDto> getMemberProfile(@PathVariable Long memberId) {
        Long patientId = getCurrentPatientId();
        DependentProfileDto profile = familyService.getMemberProfile(memberId, patientId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update a family member's medical profile
     */
    @PutMapping("/members/{memberId}/profile")
    public ResponseEntity<DependentProfileDto> updateMemberProfile(
            @PathVariable Long memberId,
            @RequestBody DependentProfileDto profileDto) {
        Long patientId = getCurrentPatientId();
        DependentProfileDto profile = familyService.updateMemberProfile(memberId, profileDto, patientId);
        return ResponseEntity.ok(profile);
    }

    // ==================== Profile Switching Endpoints ====================

    /**
     * Switch active profile to a family member
     */
    @PostMapping("/switch/{memberId}")
    public ResponseEntity<ActiveProfileContext> switchProfile(@PathVariable Long memberId) {
        Long patientId = getCurrentPatientId();
        ActiveProfileContext context = familyService.switchProfile(patientId, memberId);
        return ResponseEntity.ok(context);
    }

    /**
     * Switch back to own profile
     */
    @PostMapping("/switch/self")
    public ResponseEntity<ActiveProfileContext> switchToSelf() {
        Long patientId = getCurrentPatientId();
        ActiveProfileContext context = familyService.switchProfile(patientId, null);
        return ResponseEntity.ok(context);
    }

    /**
     * Get current active profile context
     */
    @GetMapping("/active-profile")
    public ResponseEntity<ActiveProfileContext> getActiveProfile(
            @RequestParam(required = false) Long memberId) {
        Long patientId = getCurrentPatientId();
        ActiveProfileContext context = familyService.getActiveProfileContext(patientId, memberId);
        return ResponseEntity.ok(context);
    }

    // ==================== Helper Methods ====================

    private Long getCurrentPatientId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }

        // If principal is a string (email), look up patient ID from Patient table directly
        String email = principal.toString();
        Patient patient = patientRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found for email: " + email));
        return patient.getId();
    }
}
