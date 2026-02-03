package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.ConversationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDto {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Long familyMemberId;
    private String familyMemberName;
    private String subject;
    private ConversationStatus status;
    private LocalDateTime lastMessageAt;
    private String lastMessagePreview;
    private Integer unreadCount;
    private LocalDateTime createdAt;
}
