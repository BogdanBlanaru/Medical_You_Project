package com.zega.medical_you_be.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateConversationDto {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private Long familyMemberId;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must be less than 200 characters")
    private String subject;

    @NotBlank(message = "Initial message is required")
    private String initialMessage;
}
