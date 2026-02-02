package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.RelationshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddFamilyMemberRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Relationship type is required")
    private RelationshipType relationshipType;

    private LocalDate dateOfBirth;
}
