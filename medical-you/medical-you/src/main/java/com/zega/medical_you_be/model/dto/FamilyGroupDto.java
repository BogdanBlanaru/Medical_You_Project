package com.zega.medical_you_be.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyGroupDto {
    private Long id;
    private String name;
    private Long createdById;
    private String createdByName;
    private List<FamilyMemberDto> members;
    private LocalDateTime createdAt;
}
