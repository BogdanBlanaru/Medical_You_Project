package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.*;
import com.zega.medical_you_be.model.entity.*;
import com.zega.medical_you_be.model.enums.RelationshipType;
import com.zega.medical_you_be.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FamilyServiceImpl implements FamilyService {

    private final FamilyGroupRepo familyGroupRepo;
    private final FamilyMemberRepo familyMemberRepo;
    private final DependentProfileRepo dependentProfileRepo;
    private final PatientRepo patientRepo;
    private final PatientProfileRepo patientProfileRepo;

    // ==================== Family Group Management ====================

    @Override
    @Transactional
    public FamilyGroupDto createFamilyGroup(Long patientId, String name) {
        // Check if patient already has a family group
        if (familyGroupRepo.existsByCreatedById(patientId)) {
            throw new RuntimeException("Patient already has a family group");
        }

        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Create family group
        FamilyGroup familyGroup = FamilyGroup.builder()
                .name(name != null ? name : patient.getName() + "'s Family")
                .createdBy(patient)
                .build();

        familyGroup = familyGroupRepo.save(familyGroup);

        // Create SELF member for the account owner
        FamilyMember selfMember = FamilyMember.builder()
                .familyGroup(familyGroup)
                .name(patient.getName())
                .relationshipType(RelationshipType.SELF)
                .isActive(true)
                .build();

        familyMemberRepo.save(selfMember);

        log.info("Family group created for patient {}: {}", patientId, familyGroup.getId());
        return mapToGroupDto(familyGroupRepo.findByIdWithMembers(familyGroup.getId()).orElse(familyGroup));
    }

    @Override
    public FamilyGroupDto getMyFamilyGroup(Long patientId) {
        return familyGroupRepo.findByCreatedByIdWithMembers(patientId)
                .map(this::mapToGroupDto)
                .orElse(null);
    }

    @Override
    public FamilyGroupDto getFamilyGroupById(Long groupId) {
        return familyGroupRepo.findByIdWithMembers(groupId)
                .map(this::mapToGroupDto)
                .orElseThrow(() -> new RuntimeException("Family group not found"));
    }

    // ==================== Family Member Management ====================

    @Override
    @Transactional
    public FamilyMemberDto addFamilyMember(Long patientId, AddFamilyMemberRequest request) {
        FamilyGroup familyGroup = familyGroupRepo.findByCreatedById(patientId)
                .orElseGet(() -> {
                    // Auto-create family group if doesn't exist
                    Patient patient = patientRepo.findById(patientId)
                            .orElseThrow(() -> new RuntimeException("Patient not found"));

                    FamilyGroup newGroup = FamilyGroup.builder()
                            .name(patient.getName() + "'s Family")
                            .createdBy(patient)
                            .build();
                    newGroup = familyGroupRepo.save(newGroup);

                    // Create SELF member
                    FamilyMember selfMember = FamilyMember.builder()
                            .familyGroup(newGroup)
                            .name(patient.getName())
                            .relationshipType(RelationshipType.SELF)
                            .isActive(true)
                            .build();
                    familyMemberRepo.save(selfMember);

                    return newGroup;
                });

        // Validate: cannot add another SELF
        if (request.getRelationshipType() == RelationshipType.SELF) {
            throw new RuntimeException("Cannot add another SELF member");
        }

        // Create family member
        FamilyMember member = FamilyMember.builder()
                .familyGroup(familyGroup)
                .name(request.getName())
                .relationshipType(request.getRelationshipType())
                .dateOfBirth(request.getDateOfBirth())
                .isActive(true)
                .build();

        member = familyMemberRepo.save(member);

        // Create empty dependent profile
        DependentProfile profile = DependentProfile.builder()
                .familyMember(member)
                .build();
        profile.setAllergiesList(new ArrayList<>());
        profile.setChronicConditionsList(new ArrayList<>());
        profile.setMedicationsList(new ArrayList<>());
        dependentProfileRepo.save(profile);

        log.info("Family member added: {} ({}) for patient {}",
                member.getName(), member.getRelationshipType(), patientId);

        return mapToMemberDto(member);
    }

    @Override
    @Transactional
    public FamilyMemberDto updateFamilyMember(Long memberId, AddFamilyMemberRequest request, Long patientId) {
        FamilyMember member = familyMemberRepo.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Family member not found"));

        // Verify ownership
        if (!familyMemberRepo.belongsToPatient(memberId, patientId)) {
            throw new RuntimeException("Access denied");
        }

        // Cannot change SELF relationship type
        if (member.getRelationshipType() == RelationshipType.SELF &&
            request.getRelationshipType() != RelationshipType.SELF) {
            throw new RuntimeException("Cannot change relationship type of SELF member");
        }

        member.setName(request.getName());
        if (member.getRelationshipType() != RelationshipType.SELF) {
            member.setRelationshipType(request.getRelationshipType());
        }
        member.setDateOfBirth(request.getDateOfBirth());

        member = familyMemberRepo.save(member);
        return mapToMemberDto(member);
    }

    @Override
    @Transactional
    public void removeFamilyMember(Long memberId, Long patientId) {
        FamilyMember member = familyMemberRepo.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Family member not found"));

        // Verify ownership
        if (!familyMemberRepo.belongsToPatient(memberId, patientId)) {
            throw new RuntimeException("Access denied");
        }

        // Cannot remove SELF
        if (member.getRelationshipType() == RelationshipType.SELF) {
            throw new RuntimeException("Cannot remove SELF member");
        }

        // Soft delete - mark as inactive
        member.setIsActive(false);
        familyMemberRepo.save(member);

        log.info("Family member removed (soft delete): {} for patient {}", memberId, patientId);
    }

    @Override
    public List<FamilyMemberDto> getFamilyMembers(Long patientId) {
        FamilyGroup familyGroup = familyGroupRepo.findByCreatedById(patientId)
                .orElse(null);

        if (familyGroup == null) {
            return new ArrayList<>();
        }

        return familyMemberRepo.findByFamilyGroupIdAndIsActiveTrue(familyGroup.getId())
                .stream()
                .map(this::mapToMemberDto)
                .collect(Collectors.toList());
    }

    @Override
    public FamilyMemberDto getFamilyMemberById(Long memberId, Long patientId) {
        FamilyMember member = familyMemberRepo.findByIdWithProfile(memberId)
                .orElseThrow(() -> new RuntimeException("Family member not found"));

        // Verify ownership
        if (!familyMemberRepo.belongsToPatient(memberId, patientId)) {
            throw new RuntimeException("Access denied");
        }

        return mapToMemberDto(member);
    }

    // ==================== Profile Management for Family Members ====================

    @Override
    public DependentProfileDto getMemberProfile(Long memberId, Long patientId) {
        FamilyMember member = familyMemberRepo.findByIdWithProfile(memberId)
                .orElseThrow(() -> new RuntimeException("Family member not found"));

        // Verify ownership
        if (!familyMemberRepo.belongsToPatient(memberId, patientId)) {
            throw new RuntimeException("Access denied");
        }

        // For SELF member, return patient's own profile info
        if (member.getRelationshipType() == RelationshipType.SELF) {
            return getPatientProfileAsDependent(patientId, member);
        }

        DependentProfile profile = dependentProfileRepo.findByFamilyMemberId(memberId)
                .orElseGet(() -> createEmptyDependentProfile(member));

        return mapToDependentProfileDto(member, profile);
    }

    @Override
    @Transactional
    public DependentProfileDto updateMemberProfile(Long memberId, DependentProfileDto profileDto, Long patientId) {
        FamilyMember member = familyMemberRepo.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Family member not found"));

        // Verify ownership
        if (!familyMemberRepo.belongsToPatient(memberId, patientId)) {
            throw new RuntimeException("Access denied");
        }

        // For SELF member, redirect to patient profile update
        if (member.getRelationshipType() == RelationshipType.SELF) {
            throw new RuntimeException("Use patient profile API to update SELF profile");
        }

        DependentProfile profile = dependentProfileRepo.findByFamilyMemberId(memberId)
                .orElseGet(() -> createEmptyDependentProfile(member));

        // Update profile fields
        if (profileDto.getPhoneNumber() != null) {
            profile.setPhoneNumber(profileDto.getPhoneNumber());
        }
        if (profileDto.getGender() != null) {
            profile.setGender(profileDto.getGender());
        }
        if (profileDto.getAddress() != null) {
            profile.setAddress(profileDto.getAddress());
        }
        if (profileDto.getCity() != null) {
            profile.setCity(profileDto.getCity());
        }
        if (profileDto.getCountry() != null) {
            profile.setCountry(profileDto.getCountry());
        }
        if (profileDto.getBloodType() != null) {
            profile.setBloodType(profileDto.getBloodType());
        }
        if (profileDto.getHeightCm() != null) {
            profile.setHeightCm(profileDto.getHeightCm());
        }
        if (profileDto.getWeightKg() != null) {
            profile.setWeightKg(profileDto.getWeightKg());
        }
        if (profileDto.getAllergies() != null) {
            profile.setAllergiesList(new ArrayList<>(profileDto.getAllergies()));
        }
        if (profileDto.getChronicConditions() != null) {
            profile.setChronicConditionsList(new ArrayList<>(profileDto.getChronicConditions()));
        }
        if (profileDto.getMedications() != null) {
            profile.setMedicationsList(new ArrayList<>(profileDto.getMedications()));
        }
        if (profileDto.getEmergencyContactName() != null) {
            profile.setEmergencyContactName(profileDto.getEmergencyContactName());
        }
        if (profileDto.getEmergencyContactPhone() != null) {
            profile.setEmergencyContactPhone(profileDto.getEmergencyContactPhone());
        }
        if (profileDto.getEmergencyContactRelationship() != null) {
            profile.setEmergencyContactRelationship(profileDto.getEmergencyContactRelationship());
        }

        // Update member name and date of birth if provided
        if (profileDto.getName() != null) {
            member.setName(profileDto.getName());
            familyMemberRepo.save(member);
        }
        if (profileDto.getDateOfBirth() != null) {
            member.setDateOfBirth(profileDto.getDateOfBirth());
            familyMemberRepo.save(member);
        }

        profile = dependentProfileRepo.save(profile);
        return mapToDependentProfileDto(member, profile);
    }

    // ==================== Profile Switching ====================

    @Override
    public ActiveProfileContext switchProfile(Long patientId, Long familyMemberId) {
        // If null or 0, switch to own profile
        if (familyMemberId == null || familyMemberId == 0) {
            return getOwnProfileContext(patientId);
        }

        FamilyMember member = familyMemberRepo.findByIdWithProfile(familyMemberId)
                .orElseThrow(() -> new RuntimeException("Family member not found"));

        // Verify ownership
        if (!familyMemberRepo.belongsToPatient(familyMemberId, patientId)) {
            throw new RuntimeException("Access denied");
        }

        // For SELF, return own profile context
        if (member.getRelationshipType() == RelationshipType.SELF) {
            return getOwnProfileContext(patientId);
        }

        String avatarUrl = null;
        String medicalId = null;

        if (member.getDependentProfile() != null) {
            avatarUrl = member.getDependentProfile().getAvatarUrl();
            medicalId = member.getDependentProfile().getMedicalId();
        }

        return ActiveProfileContext.builder()
                .familyMemberId(familyMemberId)
                .name(member.getName())
                .relationshipType(member.getRelationshipType())
                .isOwnProfile(false)
                .avatarUrl(avatarUrl)
                .medicalId(medicalId)
                .build();
    }

    @Override
    public ActiveProfileContext getActiveProfileContext(Long patientId, Long familyMemberId) {
        return switchProfile(patientId, familyMemberId);
    }

    // ==================== Validation ====================

    @Override
    public boolean isFamilyMemberOfPatient(Long memberId, Long patientId) {
        return familyMemberRepo.belongsToPatient(memberId, patientId);
    }

    // ==================== Helper Methods ====================

    private ActiveProfileContext getOwnProfileContext(Long patientId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        PatientProfile profile = patientProfileRepo.findByPatientId(patientId).orElse(null);

        String avatarUrl = profile != null ? profile.getAvatarUrl() : null;
        String medicalId = profile != null ? profile.getMedicalId() : null;

        // Find SELF member ID if exists
        Long selfMemberId = null;
        FamilyGroup familyGroup = familyGroupRepo.findByCreatedById(patientId).orElse(null);
        if (familyGroup != null) {
            FamilyMember selfMember = familyMemberRepo.findSelfMember(familyGroup.getId()).orElse(null);
            if (selfMember != null) {
                selfMemberId = selfMember.getId();
            }
        }

        return ActiveProfileContext.builder()
                .familyMemberId(selfMemberId)
                .name(patient.getName())
                .relationshipType(RelationshipType.SELF)
                .isOwnProfile(true)
                .avatarUrl(avatarUrl)
                .medicalId(medicalId)
                .build();
    }

    private DependentProfile createEmptyDependentProfile(FamilyMember member) {
        DependentProfile profile = DependentProfile.builder()
                .familyMember(member)
                .build();
        profile.setAllergiesList(new ArrayList<>());
        profile.setChronicConditionsList(new ArrayList<>());
        profile.setMedicationsList(new ArrayList<>());
        return dependentProfileRepo.save(profile);
    }

    private DependentProfileDto getPatientProfileAsDependent(Long patientId, FamilyMember member) {
        PatientProfile patientProfile = patientProfileRepo.findByPatientIdWithPatient(patientId)
                .orElse(null);

        if (patientProfile == null) {
            return DependentProfileDto.builder()
                    .familyMemberId(member.getId())
                    .name(member.getName())
                    .relationshipType(RelationshipType.SELF)
                    .build();
        }

        return DependentProfileDto.builder()
                .id(patientProfile.getId())
                .familyMemberId(member.getId())
                .name(patientProfile.getPatient().getName())
                .relationshipType(RelationshipType.SELF)
                .dateOfBirth(patientProfile.getDateOfBirth())
                .age(patientProfile.getAge())
                .phoneNumber(patientProfile.getPhoneNumber())
                .gender(patientProfile.getGender())
                .address(patientProfile.getAddress())
                .city(patientProfile.getCity())
                .country(patientProfile.getCountry())
                .avatarUrl(patientProfile.getAvatarUrl())
                .bloodType(patientProfile.getBloodType())
                .heightCm(patientProfile.getHeightCm())
                .weightKg(patientProfile.getWeightKg())
                .bmi(patientProfile.getBmi())
                .allergies(patientProfile.getAllergiesList())
                .chronicConditions(patientProfile.getChronicConditionsList())
                .medications(patientProfile.getMedicationsList())
                .emergencyContactName(patientProfile.getEmergencyContactName())
                .emergencyContactPhone(patientProfile.getEmergencyContactPhone())
                .emergencyContactRelationship(patientProfile.getEmergencyContactRelationship())
                .medicalId(patientProfile.getMedicalId())
                .build();
    }

    private FamilyGroupDto mapToGroupDto(FamilyGroup group) {
        List<FamilyMemberDto> memberDtos = group.getMembers() != null
                ? group.getMembers().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                    .map(this::mapToMemberDto)
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return FamilyGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .createdById(group.getCreatedBy().getId())
                .createdByName(group.getCreatedBy().getName())
                .members(memberDtos)
                .createdAt(group.getCreatedAt())
                .build();
    }

    private FamilyMemberDto mapToMemberDto(FamilyMember member) {
        String avatarUrl = null;
        String medicalId = null;

        if (member.getDependentProfile() != null) {
            avatarUrl = member.getDependentProfile().getAvatarUrl();
            medicalId = member.getDependentProfile().getMedicalId();
        }

        return FamilyMemberDto.builder()
                .id(member.getId())
                .familyGroupId(member.getFamilyGroup().getId())
                .name(member.getName())
                .relationshipType(member.getRelationshipType())
                .dateOfBirth(member.getDateOfBirth())
                .age(member.getAge())
                .isActive(member.getIsActive())
                .avatarUrl(avatarUrl)
                .medicalId(medicalId)
                .build();
    }

    private DependentProfileDto mapToDependentProfileDto(FamilyMember member, DependentProfile profile) {
        return DependentProfileDto.builder()
                .id(profile.getId())
                .familyMemberId(member.getId())
                .name(member.getName())
                .relationshipType(member.getRelationshipType())
                .dateOfBirth(member.getDateOfBirth())
                .age(member.getAge())
                .phoneNumber(profile.getPhoneNumber())
                .gender(profile.getGender())
                .address(profile.getAddress())
                .city(profile.getCity())
                .country(profile.getCountry())
                .avatarUrl(profile.getAvatarUrl())
                .bloodType(profile.getBloodType())
                .heightCm(profile.getHeightCm())
                .weightKg(profile.getWeightKg())
                .bmi(profile.getBmi())
                .allergies(profile.getAllergiesList())
                .chronicConditions(profile.getChronicConditionsList())
                .medications(profile.getMedicationsList())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactPhone(profile.getEmergencyContactPhone())
                .emergencyContactRelationship(profile.getEmergencyContactRelationship())
                .medicalId(profile.getMedicalId())
                .build();
    }
}
