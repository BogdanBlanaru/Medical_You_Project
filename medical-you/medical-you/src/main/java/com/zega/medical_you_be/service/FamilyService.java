package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.*;

import java.util.List;

public interface FamilyService {

    // Family Group Management
    FamilyGroupDto createFamilyGroup(Long patientId, String name);
    FamilyGroupDto getMyFamilyGroup(Long patientId);
    FamilyGroupDto getFamilyGroupById(Long groupId);

    // Family Member Management
    FamilyMemberDto addFamilyMember(Long patientId, AddFamilyMemberRequest request);
    FamilyMemberDto updateFamilyMember(Long memberId, AddFamilyMemberRequest request, Long patientId);
    void removeFamilyMember(Long memberId, Long patientId);
    List<FamilyMemberDto> getFamilyMembers(Long patientId);
    FamilyMemberDto getFamilyMemberById(Long memberId, Long patientId);

    // Profile Management for Family Members
    DependentProfileDto getMemberProfile(Long memberId, Long patientId);
    DependentProfileDto updateMemberProfile(Long memberId, DependentProfileDto profileDto, Long patientId);

    // Profile Switching
    ActiveProfileContext switchProfile(Long patientId, Long familyMemberId);
    ActiveProfileContext getActiveProfileContext(Long patientId, Long familyMemberId);

    // Validation
    boolean isFamilyMemberOfPatient(Long memberId, Long patientId);
}
