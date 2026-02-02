package com.zega.medical_you_be.model.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoCallMessageDto {

    private Long id;
    private String roomId;
    private String senderName;
    private Long senderId;
    private String senderType;
    private String message;
    private LocalDateTime createdAt;
}
