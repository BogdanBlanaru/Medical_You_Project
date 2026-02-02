package com.zega.medical_you_be.controller;

import com.zega.medical_you_be.model.dto.DocumentDto;
import com.zega.medical_you_be.model.dto.DocumentFolderDto;
import com.zega.medical_you_be.model.dto.UploadDocumentDto;
import com.zega.medical_you_be.model.enums.DocumentType;
import com.zega.medical_you_be.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentService documentService;

    // ==================== Document CRUD ====================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> uploadDocument(
            Authentication auth,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "familyMemberId", required = false) Long familyMemberId,
            @RequestParam(value = "folderId", required = false) Long folderId,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "documentDate", required = false) String documentDate,
            @RequestParam(value = "shareWithDoctor", required = false) Boolean shareWithDoctor) {

        UploadDocumentDto dto = UploadDocumentDto.builder()
                .documentType(documentType)
                .title(title)
                .description(description)
                .familyMemberId(familyMemberId)
                .folderId(folderId)
                .tags(tags)
                .documentDate(documentDate != null ? java.time.LocalDate.parse(documentDate) : null)
                .shareWithDoctor(shareWithDoctor)
                .build();

        log.info("Uploading document: {} for user: {}", title, auth.getName());
        DocumentDto result = documentService.uploadDocument(auth.getName(), file, dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(
            Authentication auth,
            @PathVariable Long id) {
        DocumentDto result = documentService.getDocument(auth.getName(), id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentDto> updateDocument(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody UploadDocumentDto dto) {
        DocumentDto result = documentService.updateDocument(auth.getName(), id, dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            Authentication auth,
            @PathVariable Long id) {
        documentService.deleteDocument(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Download & Preview ====================

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(
            Authentication auth,
            @PathVariable Long id) {
        DocumentDto document = documentService.getDocument(auth.getName(), id);
        byte[] content = documentService.downloadDocument(auth.getName(), id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, document.getMimeType())
                .body(content);
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<byte[]> getDocumentThumbnail(
            Authentication auth,
            @PathVariable Long id) {
        byte[] thumbnail = documentService.getDocumentThumbnail(auth.getName(), id);
        if (thumbnail == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(thumbnail);
    }

    // ==================== Document Listing ====================

    @GetMapping
    public ResponseEntity<Page<DocumentDto>> getDocuments(
            Authentication auth,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) DocumentType type,
            @RequestParam(required = false) Long familyMemberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<DocumentDto> result = documentService.getDocuments(auth.getName(), folderId, type, familyMemberId, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DocumentDto>> searchDocuments(
            Authentication auth,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<DocumentDto> result = documentService.searchDocuments(auth.getName(), query, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<DocumentDto>> getRecentDocuments(Authentication auth) {
        List<DocumentDto> result = documentService.getRecentDocuments(auth.getName());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/shared")
    public ResponseEntity<List<DocumentDto>> getSharedDocuments(Authentication auth) {
        List<DocumentDto> result = documentService.getSharedDocuments(auth.getName());
        return ResponseEntity.ok(result);
    }

    // ==================== Folder Operations ====================

    @PostMapping("/folders")
    public ResponseEntity<DocumentFolderDto> createFolder(
            Authentication auth,
            @Valid @RequestBody DocumentFolderDto dto) {
        log.info("Creating folder: {} for user: {}", dto.getName(), auth.getName());
        DocumentFolderDto result = documentService.createFolder(auth.getName(), dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/folders/{id}")
    public ResponseEntity<DocumentFolderDto> getFolder(
            Authentication auth,
            @PathVariable Long id) {
        DocumentFolderDto result = documentService.getFolder(auth.getName(), id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/folders/{id}")
    public ResponseEntity<DocumentFolderDto> updateFolder(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody DocumentFolderDto dto) {
        DocumentFolderDto result = documentService.updateFolder(auth.getName(), id, dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/folders/{id}")
    public ResponseEntity<Void> deleteFolder(
            Authentication auth,
            @PathVariable Long id) {
        documentService.deleteFolder(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/folders")
    public ResponseEntity<List<DocumentFolderDto>> getFolders(Authentication auth) {
        List<DocumentFolderDto> result = documentService.getFolders(auth.getName());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/folders/tree")
    public ResponseEntity<List<DocumentFolderDto>> getFolderTree(Authentication auth) {
        List<DocumentFolderDto> result = documentService.getFolderTree(auth.getName());
        return ResponseEntity.ok(result);
    }

    // ==================== Sharing ====================

    @PostMapping("/{id}/share/{doctorId}")
    public ResponseEntity<DocumentDto> shareWithDoctor(
            Authentication auth,
            @PathVariable Long id,
            @PathVariable Long doctorId) {
        DocumentDto result = documentService.shareWithDoctor(auth.getName(), id, doctorId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}/share/{doctorId}")
    public ResponseEntity<DocumentDto> unshareWithDoctor(
            Authentication auth,
            @PathVariable Long id,
            @PathVariable Long doctorId) {
        DocumentDto result = documentService.unshareWithDoctor(auth.getName(), id, doctorId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/toggle-share")
    public ResponseEntity<DocumentDto> toggleShareWithDoctor(
            Authentication auth,
            @PathVariable Long id) {
        DocumentDto result = documentService.toggleShareWithDoctor(auth.getName(), id);
        return ResponseEntity.ok(result);
    }

    // ==================== Statistics ====================

    @GetMapping("/stats")
    public ResponseEntity<DocumentService.DocumentStats> getStats(Authentication auth) {
        DocumentService.DocumentStats result = documentService.getStats(auth.getName());
        return ResponseEntity.ok(result);
    }

    // ==================== Document Types ====================

    @GetMapping("/types")
    public ResponseEntity<DocumentType[]> getDocumentTypes() {
        return ResponseEntity.ok(DocumentType.values());
    }
}
