package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.RelationshipType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveProfileContext {
    private Long familyMemberId;
    private String name;
    private RelationshipType relationshipType;
    private Boolean isOwnProfile;
    private String avatarUrl;
    private String medicalId;
}
