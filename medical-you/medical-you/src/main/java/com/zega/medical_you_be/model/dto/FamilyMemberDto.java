package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.RelationshipType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberDto {
    private Long id;
    private Long familyGroupId;
    private String name;
    private RelationshipType relationshipType;
    private LocalDate dateOfBirth;
    private Integer age;
    private Boolean isActive;
    private String avatarUrl;
    private String medicalId;
}
