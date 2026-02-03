package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.*;
import com.zega.medical_you_be.model.enums.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface ConversationService {

    ConversationDto createConversation(String userEmail, CreateConversationDto dto);

    ConversationDto getConversation(String userEmail, Long conversationId);

    Page<ConversationDto> getPatientConversations(String patientEmail, ConversationStatus status, Pageable pageable);

    Page<ConversationDto> getDoctorConversations(String doctorEmail, ConversationStatus status, Pageable pageable);

    ConversationDto updateConversationStatus(String userEmail, Long conversationId, ConversationStatus status);

    int getTotalUnreadCount(String userEmail);

    ChatMessageDto sendMessage(String userEmail, SendMessageDto dto);

    ChatMessageDto sendMessageWithAttachment(String userEmail, Long conversationId, String content, MultipartFile file);

    Page<ChatMessageDto> getMessages(String userEmail, Long conversationId, Pageable pageable);

    List<ChatMessageDto> getNewMessages(String userEmail, Long conversationId, LocalDateTime since);

    void markAsRead(String userEmail, Long conversationId);

    List<AssignedDoctorDto> getAssignedDoctors(String patientEmail);
}
