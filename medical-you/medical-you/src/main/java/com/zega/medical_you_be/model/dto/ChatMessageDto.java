package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderType;
    private String senderName;
    private MessageType messageType;
    private String content;
    private String attachmentUrl;
    private String attachmentName;
    private Long attachmentSize;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
