package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.*;
import com.zega.medical_you_be.model.entity.*;
import com.zega.medical_you_be.model.enums.ConversationStatus;
import com.zega.medical_you_be.model.enums.MessageType;
import com.zega.medical_you_be.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepo conversationRepo;
    private final ChatMessageRepo messageRepo;
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;
    private final FamilyMemberRepo familyMemberRepo;
    private final AppointmentRepository appointmentRepo;
    private final EmailService emailService;

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    // ==================== Conversation Management ====================

    @Override
    @Transactional
    public ConversationDto createConversation(String userEmail, CreateConversationDto dto) {
        Patient patient = patientRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor doctor = doctorRepo.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        FamilyMember familyMember = null;
        if (dto.getFamilyMemberId() != null) {
            familyMember = familyMemberRepo.findByIdAndFamilyGroupPatientId(dto.getFamilyMemberId(), patient.getId())
                    .orElseThrow(() -> new RuntimeException("Family member not found"));
        }

        Conversation conversation = Conversation.builder()
                .patient(patient)
                .doctor(doctor)
                .familyMember(familyMember)
                .subject(dto.getSubject())
                .status(ConversationStatus.ACTIVE)
                .build();

        conversation = conversationRepo.save(conversation);

        // Create initial message
        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .senderId(patient.getId())
                .senderType("PATIENT")
                .senderName(patient.getName())
                .messageType(MessageType.TEXT)
                .content(dto.getInitialMessage())
                .build();

        message = messageRepo.save(message);

        // Update conversation metadata
        conversation.setLastMessageAt(message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now());
        conversation.setLastMessagePreview(truncatePreview(dto.getInitialMessage()));
        conversation.setUnreadCountDoctor(1);
        conversationRepo.save(conversation);

        // Send email notification to doctor
        sendNewMessageNotification(conversation, message);

        log.info("Created conversation {} between patient {} and doctor {}",
                conversation.getId(), patient.getEmail(), doctor.getEmail());

        return mapToDto(conversation, "PATIENT");
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDto getConversation(String userEmail, Long conversationId) {
        // Try as patient first
        Patient patient = patientRepo.findByEmail(userEmail).orElse(null);
        if (patient != null) {
            Optional<Conversation> conv = conversationRepo.findByIdAndPatientId(conversationId, patient.getId());
            if (conv.isPresent()) return mapToDto(conv.get(), "PATIENT");
        }

        // Try as doctor (handles same email in both tables)
        Doctor doctor = doctorRepo.findByEmail(userEmail).orElse(null);
        if (doctor != null) {
            Optional<Conversation> conv = conversationRepo.findByIdAndDoctorId(conversationId, doctor.getId());
            if (conv.isPresent()) return mapToDto(conv.get(), "DOCTOR");
        }

        if (patient == null && doctor == null) throw new RuntimeException("User not found");
        throw new RuntimeException("Conversation not found");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationDto> getPatientConversations(String patientEmail, ConversationStatus status, Pageable pageable) {
        Patient patient = patientRepo.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return conversationRepo.findByPatientIdAndStatus(patient.getId(), status, pageable)
                .map(c -> mapToDto(c, "PATIENT"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationDto> getDoctorConversations(String doctorEmail, ConversationStatus status, Pageable pageable) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return conversationRepo.findByDoctorIdAndStatus(doctor.getId(), status, pageable)
                .map(c -> mapToDto(c, "DOCTOR"));
    }

    @Override
    @Transactional
    public ConversationDto updateConversationStatus(String userEmail, Long conversationId, ConversationStatus status) {
        Conversation conversation = getConversationWithAccess(userEmail, conversationId);
        conversation.setStatus(status);
        conversationRepo.save(conversation);

        log.info("Updated conversation {} status to {}", conversationId, status);
        return mapToDto(conversation, getUserType(userEmail, conversationId));
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalUnreadCount(String userEmail) {
        Patient patient = patientRepo.findByEmail(userEmail).orElse(null);
        if (patient != null) {
            return conversationRepo.countTotalUnreadForPatient(patient.getId());
        }

        Doctor doctor = doctorRepo.findByEmail(userEmail).orElse(null);
        if (doctor != null) {
            return conversationRepo.countTotalUnreadForDoctor(doctor.getId());
        }

        return 0;
    }

    // ==================== Message Management ====================

    @Override
    @Transactional
    public ChatMessageDto sendMessage(String userEmail, SendMessageDto dto) {
        Conversation conversation = getConversationWithAccess(userEmail, dto.getConversationId());

        if (conversation.getStatus() == ConversationStatus.CLOSED) {
            throw new RuntimeException("Cannot send messages to a closed conversation");
        }

        String userType = getUserType(userEmail, dto.getConversationId());
        String senderName = getSenderName(userEmail, userType);
        Long senderId = getSenderId(userEmail, userType);

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .senderId(senderId)
                .senderType(userType)
                .senderName(senderName)
                .messageType(dto.getMessageType() != null ? dto.getMessageType() : MessageType.TEXT)
                .content(dto.getContent())
                .attachmentUrl(dto.getAttachmentUrl())
                .attachmentName(dto.getAttachmentName())
                .attachmentSize(dto.getAttachmentSize())
                .build();

        message = messageRepo.save(message);

        // Update conversation
        conversation.setLastMessageAt(message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now());
        conversation.setLastMessagePreview(truncatePreview(dto.getContent()));
        conversation.incrementUnreadCount(userType);
        conversationRepo.save(conversation);

        // Send email notification
        sendNewMessageNotification(conversation, message);

        return mapToMessageDto(message);
    }

    @Override
    @Transactional
    public ChatMessageDto sendMessageWithAttachment(String userEmail, Long conversationId, String content,
                                                     MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadPath, "chat-attachments", fileName);

        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save attachment", e);
        }

        SendMessageDto dto = SendMessageDto.builder()
                .conversationId(conversationId)
                .content(content != null && !content.isBlank() ? content : file.getOriginalFilename())
                .messageType(determineMessageType(file.getContentType()))
                .attachmentUrl("/api/conversations/attachments/" + fileName)
                .attachmentName(file.getOriginalFilename())
                .attachmentSize(file.getSize())
                .build();

        return sendMessage(userEmail, dto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageDto> getMessages(String userEmail, Long conversationId, Pageable pageable) {
        getConversationWithAccess(userEmail, conversationId);
        return messageRepo.findByConversationId(conversationId, pageable)
                .map(this::mapToMessageDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getNewMessages(String userEmail, Long conversationId, LocalDateTime since) {
        getConversationWithAccess(userEmail, conversationId);
        return messageRepo.findNewMessages(conversationId, since).stream()
                .map(this::mapToMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(String userEmail, Long conversationId) {
        getConversationWithAccess(userEmail, conversationId);
        String userType = getUserType(userEmail, conversationId);

        messageRepo.markMessagesAsRead(conversationId, userType, LocalDateTime.now());

        if ("PATIENT".equals(userType)) {
            conversationRepo.resetPatientUnreadCount(conversationId);
        } else {
            conversationRepo.resetDoctorUnreadCount(conversationId);
        }
    }

    // ==================== Assigned Doctors ====================

    @Override
    @Transactional(readOnly = true)
    public List<AssignedDoctorDto> getAssignedDoctors(String patientEmail) {
        Patient patient = patientRepo.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Collect doctors from DoctorPatient relationships
        Map<Long, AssignedDoctorDto> doctorMap = new LinkedHashMap<>();

        patient.getDoctors().stream()
                .filter(dp -> !dp.getIsDeleted())
                .forEach(dp -> doctorMap.put(dp.getDoctor().getId(), AssignedDoctorDto.builder()
                        .id(dp.getDoctor().getId())
                        .name(dp.getDoctor().getName())
                        .email(dp.getDoctor().getEmail())
                        .specialization(dp.getDoctor().getSpecialization())
                        .hospital(dp.getDoctor().getHospital())
                        .rating(dp.getDoctor().getRating())
                        .build()));

        // Also include doctors from appointments (past and future)
        appointmentRepo.findByPatientIdAndIsCancelledFalse(patient.getId()).stream()
                .map(Appointment::getDoctor)
                .filter(doctor -> !doctorMap.containsKey(doctor.getId()))
                .forEach(doctor -> doctorMap.put(doctor.getId(), AssignedDoctorDto.builder()
                        .id(doctor.getId())
                        .name(doctor.getName())
                        .email(doctor.getEmail())
                        .specialization(doctor.getSpecialization())
                        .hospital(doctor.getHospital())
                        .rating(doctor.getRating())
                        .build()));

        return new ArrayList<>(doctorMap.values());
    }

    // ==================== Private Helpers ====================

    private Conversation getConversationWithAccess(String userEmail, Long conversationId) {
        // Try as patient first
        Patient patient = patientRepo.findByEmail(userEmail).orElse(null);
        if (patient != null) {
            Optional<Conversation> conv = conversationRepo.findByIdAndPatientId(conversationId, patient.getId());
            if (conv.isPresent()) return conv.get();
        }

        // Try as doctor (also handles case where same email exists in both tables)
        Doctor doctor = doctorRepo.findByEmail(userEmail).orElse(null);
        if (doctor != null) {
            Optional<Conversation> conv = conversationRepo.findByIdAndDoctorId(conversationId, doctor.getId());
            if (conv.isPresent()) return conv.get();
        }

        if (patient == null && doctor == null) {
            throw new RuntimeException("User not found");
        }
        throw new RuntimeException("Conversation not found");
    }

    private String getUserType(String userEmail, Long conversationId) {
        // Determine role based on conversation ownership, not just email lookup
        if (conversationId != null) {
            Patient patient = patientRepo.findByEmail(userEmail).orElse(null);
            if (patient != null && conversationRepo.findByIdAndPatientId(conversationId, patient.getId()).isPresent()) {
                return "PATIENT";
            }
            Doctor doctor = doctorRepo.findByEmail(userEmail).orElse(null);
            if (doctor != null && conversationRepo.findByIdAndDoctorId(conversationId, doctor.getId()).isPresent()) {
                return "DOCTOR";
            }
        }
        // Fallback: simple lookup (for methods without conversation context)
        if (patientRepo.findByEmail(userEmail).isPresent()) return "PATIENT";
        if (doctorRepo.findByEmail(userEmail).isPresent()) return "DOCTOR";
        throw new RuntimeException("User not found");
    }

    private String getSenderName(String userEmail, String userType) {
        if ("PATIENT".equals(userType)) {
            return patientRepo.findByEmail(userEmail).map(Patient::getName).orElse("Unknown");
        }
        return doctorRepo.findByEmail(userEmail).map(Doctor::getName).orElse("Unknown");
    }

    private Long getSenderId(String userEmail, String userType) {
        if ("PATIENT".equals(userType)) {
            return patientRepo.findByEmail(userEmail).map(Patient::getId).orElse(null);
        }
        return doctorRepo.findByEmail(userEmail).map(Doctor::getId).orElse(null);
    }

    private String truncatePreview(String content) {
        if (content == null) return "";
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }

    private MessageType determineMessageType(String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return MessageType.IMAGE;
        }
        return MessageType.FILE;
    }

    private void sendNewMessageNotification(Conversation conversation, ChatMessage message) {
        try {
            if ("PATIENT".equals(message.getSenderType())) {
                emailService.sendNewChatMessageEmail(
                        conversation.getDoctor().getEmail(),
                        conversation.getDoctor().getName(),
                        conversation.getPatient().getName(),
                        conversation.getSubject(),
                        message.getContent()
                );
            } else {
                emailService.sendNewChatMessageEmail(
                        conversation.getPatient().getEmail(),
                        conversation.getPatient().getName(),
                        conversation.getDoctor().getName(),
                        conversation.getSubject(),
                        message.getContent()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send chat notification email: {}", e.getMessage());
        }
    }

    private ConversationDto mapToDto(Conversation c, String viewerType) {
        return ConversationDto.builder()
                .id(c.getId())
                .patientId(c.getPatient().getId())
                .patientName(c.getPatient().getName())
                .doctorId(c.getDoctor().getId())
                .doctorName(c.getDoctor().getName())
                .doctorSpecialization(c.getDoctor().getSpecialization())
                .familyMemberId(c.getFamilyMember() != null ? c.getFamilyMember().getId() : null)
                .familyMemberName(c.getFamilyMember() != null ? c.getFamilyMember().getName() : null)
                .subject(c.getSubject())
                .status(c.getStatus())
                .lastMessageAt(c.getLastMessageAt())
                .lastMessagePreview(c.getLastMessagePreview())
                .unreadCount("PATIENT".equals(viewerType) ? c.getUnreadCountPatient() : c.getUnreadCountDoctor())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private ChatMessageDto mapToMessageDto(ChatMessage m) {
        return ChatMessageDto.builder()
                .id(m.getId())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSenderId())
                .senderType(m.getSenderType())
                .senderName(m.getSenderName())
                .messageType(m.getMessageType())
                .content(m.getContent())
                .attachmentUrl(m.getAttachmentUrl())
                .attachmentName(m.getAttachmentName())
                .attachmentSize(m.getAttachmentSize())
                .isRead(m.getIsRead())
                .readAt(m.getReadAt())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
