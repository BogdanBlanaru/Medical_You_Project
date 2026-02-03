package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageDto {
    private Long conversationId;

    @NotBlank(message = "Message content is required")
    private String content;

    private MessageType messageType;

    private String attachmentUrl;
    private String attachmentName;
    private Long attachmentSize;
}
