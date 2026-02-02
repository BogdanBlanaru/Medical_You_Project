package com.zega.medical_you_be.service.impl;

import com.zega.medical_you_be.model.dto.DocumentDto;
import com.zega.medical_you_be.model.dto.DocumentFolderDto;
import com.zega.medical_you_be.model.dto.UploadDocumentDto;
import com.zega.medical_you_be.model.entity.Document;
import com.zega.medical_you_be.model.entity.DocumentFolder;
import com.zega.medical_you_be.model.entity.FamilyMember;
import com.zega.medical_you_be.model.entity.Patient;
import com.zega.medical_you_be.model.enums.DocumentType;
import com.zega.medical_you_be.repo.DocumentFolderRepo;
import com.zega.medical_you_be.repo.DocumentRepo;
import com.zega.medical_you_be.repo.FamilyMemberRepo;
import com.zega.medical_you_be.repo.PatientRepo;
import com.zega.medical_you_be.service.DocumentService;
import com.zega.medical_you_be.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepo documentRepo;
    private final DocumentFolderRepo folderRepo;
    private final PatientRepo patientRepo;
    private final FamilyMemberRepo familyMemberRepo;
    private final FileStorageService fileStorageService;

    // ==================== Document Operations ====================

    @Override
    public DocumentDto uploadDocument(String username, MultipartFile file, UploadDocumentDto dto) {
        Patient patient = getPatientByUsername(username);

        // Store the file
        String filePath = fileStorageService.storeFile(file, patient.getId());

        // Generate thumbnail for images
        String thumbnailPath = fileStorageService.generateThumbnail(filePath, file.getContentType());

        // Get family member if specified
        FamilyMember familyMember = null;
        if (dto.getFamilyMemberId() != null) {
            familyMember = familyMemberRepo.findByIdAndFamilyGroupPatientId(dto.getFamilyMemberId(), patient.getId())
                    .orElseThrow(() -> new RuntimeException("Family member not found"));
        }

        // Get folder if specified
        DocumentFolder folder = null;
        if (dto.getFolderId() != null) {
            folder = folderRepo.findByIdAndPatientId(dto.getFolderId(), patient.getId())
                    .orElseThrow(() -> new RuntimeException("Folder not found"));
        }

        Document document = Document.builder()
                .patient(patient)
                .familyMember(familyMember)
                .folder(folder)
                .documentType(dto.getDocumentType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .thumbnailPath(thumbnailPath)
                .tags(dto.getTags() != null ? dto.getTags() : new ArrayList<>())
                .isSharedWithDoctor(dto.getShareWithDoctor() != null && dto.getShareWithDoctor())
                .documentDate(dto.getDocumentDate())
                .build();

        document = documentRepo.save(document);
        log.info("Uploaded document: {} for patient: {}", document.getId(), patient.getId());

        return mapToDto(document);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto getDocument(String username, Long documentId) {
        Patient patient = getPatientByUsername(username);
        Document document = documentRepo.findByIdAndPatientId(documentId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return mapToDto(document);
    }

    @Override
    public DocumentDto updateDocument(String username, Long documentId, UploadDocumentDto dto) {
        Patient patient = getPatientByUsername(username);
        Document document = documentRepo.findByIdAndPatientId(documentId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Update family member
        if (dto.getFamilyMemberId() != null) {
            FamilyMember familyMember = familyMemberRepo.findByIdAndFamilyGroupPatientId(dto.getFamilyMemberId(), patient.getId())
                    .orElseThrow(() -> new RuntimeException("Family member not found"));
            document.setFamilyMember(familyMember);
        } else {
            document.setFamilyMember(null);
        }

        // Update folder
        if (dto.getFolderId() != null) {
            DocumentFolder folder = folderRepo.findByIdAndPatientId(dto.getFolderId(), patient.getId())
                    .orElseThrow(() -> new RuntimeException("Folder not found"));
            document.setFolder(folder);
        } else {
            document.setFolder(null);
        }

        document.setDocumentType(dto.getDocumentType());
        document.setTitle(dto.getTitle());
        document.setDescription(dto.getDescription());
        document.setTags(dto.getTags() != null ? dto.getTags() : new ArrayList<>());
        document.setDocumentDate(dto.getDocumentDate());

        if (dto.getShareWithDoctor() != null) {
            document.setIsSharedWithDoctor(dto.getShareWithDoctor());
        }

        document = documentRepo.save(document);
        log.info("Updated document: {}", documentId);

        return mapToDto(document);
    }

    @Override
    public void deleteDocument(String username, Long documentId) {
        Patient patient = getPatientByUsername(username);
        Document document = documentRepo.findByIdAndPatientId(documentId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Delete file from storage
        fileStorageService.deleteFile(document.getFilePath());

        documentRepo.delete(document);
        log.info("Deleted document: {}", documentId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(String username, Long documentId) {
        Patient patient = getPatientByUsername(username);
        Document document = documentRepo.findByIdAndPatientId(documentId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        return fileStorageService.loadFile(document.getFilePath());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getDocumentThumbnail(String username, Long documentId) {
        Patient patient = getPatientByUsername(username);
        Document document = documentRepo.findByIdAndPatientId(documentId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (document.getThumbnailPath() == null) {
            return null;
        }

        return fileStorageService.loadThumbnail(document.getThumbnailPath());
    }

    // ==================== Document Listing ====================

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDto> getDocuments(String username, Long folderId, DocumentType type,
                                          Long familyMemberId, Pageable pageable) {
        Patient patient = getPatientByUsername(username);
        Page<Document> documents = documentRepo.findWithFilters(
                patient.getId(), folderId, type, familyMemberId, pageable);
        return documents.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDto> searchDocuments(String username, String query, Pageable pageable) {
        Patient patient = getPatientByUsername(username);
        Page<Document> documents = documentRepo.searchDocuments(patient.getId(), query, pageable);
        return documents.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDto> getRecentDocuments(String username) {
        Patient patient = getPatientByUsername(username);
        return documentRepo.findTop10ByPatientIdOrderByUploadedAtDesc(patient.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDto> getSharedDocuments(String username) {
        Patient patient = getPatientByUsername(username);
        return documentRepo.findSharedWithDoctor(patient.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ==================== Folder Operations ====================

    @Override
    public DocumentFolderDto createFolder(String username, DocumentFolderDto dto) {
        Patient patient = getPatientByUsername(username);

        // Check for duplicate name in same parent
        if (folderRepo.existsByPatientIdAndNameAndParentFolderId(
                patient.getId(), dto.getName(), dto.getParentFolderId())) {
            throw new RuntimeException("A folder with this name already exists");
        }

        DocumentFolder parentFolder = null;
        if (dto.getParentFolderId() != null) {
            parentFolder = folderRepo.findByIdAndPatientId(dto.getParentFolderId(), patient.getId())
                    .orElseThrow(() -> new RuntimeException("Parent folder not found"));
        }

        DocumentFolder folder = DocumentFolder.builder()
                .patient(patient)
                .name(dto.getName())
                .parentFolder(parentFolder)
                .icon(dto.getIcon())
                .color(dto.getColor())
                .build();

        folder = folderRepo.save(folder);
        log.info("Created folder: {} for patient: {}", folder.getId(), patient.getId());

        return mapToFolderDto(folder);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentFolderDto getFolder(String username, Long folderId) {
        Patient patient = getPatientByUsername(username);
        DocumentFolder folder = folderRepo.findByIdAndPatientId(folderId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        return mapToFolderDto(folder);
    }

    @Override
    public DocumentFolderDto updateFolder(String username, Long folderId, DocumentFolderDto dto) {
        Patient patient = getPatientByUsername(username);
        DocumentFolder folder = folderRepo.findByIdAndPatientId(folderId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        folder.setName(dto.getName());
        folder.setIcon(dto.getIcon());
        folder.setColor(dto.getColor());

        folder = folderRepo.save(folder);
        log.info("Updated folder: {}", folderId);

        return mapToFolderDto(folder);
    }

    @Override
    public void deleteFolder(String username, Long folderId) {
        Patient patient = getPatientByUsername(username);
        DocumentFolder folder = folderRepo.findByIdAndPatientId(folderId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        // Delete all documents in folder
        for (Document document : folder.getDocuments()) {
            fileStorageService.deleteFile(document.getFilePath());
        }

        folderRepo.delete(folder);
        log.info("Deleted folder: {}", folderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentFolderDto> getFolders(String username) {
        Patient patient = getPatientByUsername(username);
        return folderRepo.findByPatientIdOrderByNameAsc(patient.getId())
                .stream()
                .map(this::mapToFolderDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentFolderDto> getFolderTree(String username) {
        Patient patient = getPatientByUsername(username);
        List<DocumentFolder> rootFolders = folderRepo.findByPatientIdAndParentFolderIsNullOrderByNameAsc(patient.getId());
        return rootFolders.stream()
                .map(this::mapToFolderDtoWithChildren)
                .collect(Collectors.toList());
    }

    // ==================== Sharing ====================

    @Override
    public DocumentDto shareWithDoctor(String username, Long documentId, Long doctorId) {
        Patient patient = getPatientByUsername(username);
        Document document = documentRepo.findByIdAndPatientId(documentId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getSharedWithDoctors().contains(doctorId)) {
            document.getSharedWithDoctors().add(doctorId);
            document.setIsSharedWithDoctor(true);
            document = documentRepo.save(document);
        }

        return mapToDto(document);
    }

    @Override
    public DocumentDto unshareWithDoctor(String username, Long documentId, Long doctorId) {
        Patient patient = getPatientByUsername(username);
        Document document = documentRepo.findByIdAndPatientId(documentId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.getSharedWithDoctors().remove(doctorId);
        if (document.getSharedWithDoctors().isEmpty()) {
            document.setIsSharedWithDoctor(false);
        }
        document = documentRepo.save(document);

        return mapToDto(document);
    }

    @Override
    public DocumentDto toggleShareWithDoctor(String username, Long documentId) {
        Patient patient = getPatientByUsername(username);
        Document document = documentRepo.findByIdAndPatientId(documentId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setIsSharedWithDoctor(!document.getIsSharedWithDoctor());
        document = documentRepo.save(document);

        return mapToDto(document);
    }

    // ==================== Statistics ====================

    @Override
    @Transactional(readOnly = true)
    public DocumentStats getStats(String username) {
        Patient patient = getPatientByUsername(username);

        long totalDocuments = documentRepo.countByPatientId(patient.getId());
        Long totalSize = documentRepo.sumFileSizeByPatientId(patient.getId());
        if (totalSize == null) totalSize = 0L;

        Map<DocumentType, Long> countByType = new EnumMap<>(DocumentType.class);
        for (Object[] row : documentRepo.countByDocumentType(patient.getId())) {
            countByType.put((DocumentType) row[0], (Long) row[1]);
        }

        int folderCount = folderRepo.findByPatientIdOrderByNameAsc(patient.getId()).size();

        return new DocumentStats(
                totalDocuments,
                totalSize,
                formatFileSize(totalSize),
                countByType,
                folderCount
        );
    }

    // ==================== Helper Methods ====================

    private Patient getPatientByUsername(String username) {
        return patientRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    private DocumentDto mapToDto(Document document) {
        return DocumentDto.builder()
                .id(document.getId())
                .patientId(document.getPatient().getId())
                .familyMemberId(document.getFamilyMember() != null ? document.getFamilyMember().getId() : null)
                .familyMemberName(document.getFamilyMember() != null ? document.getFamilyMember().getName() : null)
                .folderId(document.getFolder() != null ? document.getFolder().getId() : null)
                .folderName(document.getFolder() != null ? document.getFolder().getName() : null)
                .documentType(document.getDocumentType())
                .title(document.getTitle())
                .description(document.getDescription())
                .fileName(document.getFileName())
                .fileSize(document.getFileSize())
                .fileSizeFormatted(document.getFileSizeFormatted())
                .mimeType(document.getMimeType())
                .thumbnailUrl(document.getThumbnailPath() != null ? "/api/documents/" + document.getId() + "/thumbnail" : null)
                .tags(document.getTags())
                .isSharedWithDoctor(document.getIsSharedWithDoctor())
                .sharedWithDoctors(document.getSharedWithDoctors())
                .documentDate(document.getDocumentDate())
                .uploadedAt(document.getUploadedAt())
                .updatedAt(document.getUpdatedAt())
                .isImage(document.isImage())
                .isPdf(document.isPdf())
                .fileExtension(document.getFileExtension())
                .build();
    }

    private DocumentFolderDto mapToFolderDto(DocumentFolder folder) {
        return DocumentFolderDto.builder()
                .id(folder.getId())
                .patientId(folder.getPatient().getId())
                .name(folder.getName())
                .parentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null)
                .parentFolderName(folder.getParentFolder() != null ? folder.getParentFolder().getName() : null)
                .icon(folder.getIcon())
                .color(folder.getColor())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .documentCount(folderRepo.countDocumentsByFolderId(folder.getId()))
                .build();
    }

    private DocumentFolderDto mapToFolderDtoWithChildren(DocumentFolder folder) {
        DocumentFolderDto dto = mapToFolderDto(folder);
        dto.setSubFolders(folder.getSubFolders().stream()
                .map(this::mapToFolderDtoWithChildren)
                .collect(Collectors.toList()));
        return dto;
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024L * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
}
