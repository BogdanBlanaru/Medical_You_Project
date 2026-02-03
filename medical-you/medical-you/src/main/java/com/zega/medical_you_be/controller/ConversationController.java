package com.zega.medical_you_be.controller;

import com.zega.medical_you_be.model.dto.*;
import com.zega.medical_you_be.model.enums.ConversationStatus;
import com.zega.medical_you_be.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ConversationController {

    private final ConversationService conversationService;

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    // ==================== Conversation Endpoints ====================

    @PostMapping
    public ResponseEntity<ConversationDto> createConversation(
            Authentication auth,
            @Valid @RequestBody CreateConversationDto dto) {
        log.info("Creating conversation for user: {}", auth.getName());
        ConversationDto result = conversationService.createConversation(auth.getName(), dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDto> getConversation(
            Authentication auth,
            @PathVariable Long id) {
        ConversationDto result = conversationService.getConversation(auth.getName(), id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/patient")
    public ResponseEntity<Page<ConversationDto>> getPatientConversations(
            Authentication auth,
            @RequestParam(required = false) ConversationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
        Page<ConversationDto> result = conversationService.getPatientConversations(auth.getName(), status, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/doctor")
    public ResponseEntity<Page<ConversationDto>> getDoctorConversations(
            Authentication auth,
            @RequestParam(required = false) ConversationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
        Page<ConversationDto> result = conversationService.getDoctorConversations(auth.getName(), status, pageRequest);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ConversationDto> updateConversationStatus(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        ConversationStatus status = ConversationStatus.valueOf(body.get("status"));
        ConversationDto result = conversationService.updateConversationStatus(auth.getName(), id, status);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(Authentication auth) {
        int count = conversationService.getTotalUnreadCount(auth.getName());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // ==================== Message Endpoints ====================

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            Authentication auth,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageDto dto) {
        dto.setConversationId(conversationId);
        ChatMessageDto result = conversationService.sendMessage(auth.getName(), dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/{conversationId}/messages/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessageDto> sendMessageWithAttachment(
            Authentication auth,
            @PathVariable Long conversationId,
            @RequestParam(required = false) String content,
            @RequestParam("file") MultipartFile file) {
        ChatMessageDto result = conversationService.sendMessageWithAttachment(
                auth.getName(), conversationId, content, file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Page<ChatMessageDto>> getMessages(
            Authentication auth,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ChatMessageDto> result = conversationService.getMessages(auth.getName(), conversationId, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{conversationId}/messages/new")
    public ResponseEntity<List<ChatMessageDto>> getNewMessages(
            Authentication auth,
            @PathVariable Long conversationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        List<ChatMessageDto> result = conversationService.getNewMessages(auth.getName(), conversationId, since);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            Authentication auth,
            @PathVariable Long conversationId) {
        conversationService.markAsRead(auth.getName(), conversationId);
        return ResponseEntity.ok().build();
    }

    // ==================== Assigned Doctors ====================

    @GetMapping("/assigned-doctors")
    public ResponseEntity<List<AssignedDoctorDto>> getAssignedDoctors(Authentication auth) {
        List<AssignedDoctorDto> result = conversationService.getAssignedDoctors(auth.getName());
        return ResponseEntity.ok(result);
    }

    // ==================== Attachment Download ====================

    @GetMapping("/attachments/{fileName:.+}")
    public ResponseEntity<Resource> getAttachment(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadPath, "chat-attachments", fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
