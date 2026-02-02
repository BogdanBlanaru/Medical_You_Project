package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.dto.DocumentDto;
import com.zega.medical_you_be.model.dto.DocumentFolderDto;
import com.zega.medical_you_be.model.dto.UploadDocumentDto;
import com.zega.medical_you_be.model.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DocumentService {

    // ==================== Document Operations ====================

    DocumentDto uploadDocument(String username, MultipartFile file, UploadDocumentDto dto);

    DocumentDto getDocument(String username, Long documentId);

    DocumentDto updateDocument(String username, Long documentId, UploadDocumentDto dto);

    void deleteDocument(String username, Long documentId);

    byte[] downloadDocument(String username, Long documentId);

    byte[] getDocumentThumbnail(String username, Long documentId);

    // ==================== Document Listing ====================

    Page<DocumentDto> getDocuments(String username, Long folderId, DocumentType type,
                                   Long familyMemberId, Pageable pageable);

    Page<DocumentDto> searchDocuments(String username, String query, Pageable pageable);

    List<DocumentDto> getRecentDocuments(String username);

    List<DocumentDto> getSharedDocuments(String username);

    // ==================== Folder Operations ====================

    DocumentFolderDto createFolder(String username, DocumentFolderDto dto);

    DocumentFolderDto getFolder(String username, Long folderId);

    DocumentFolderDto updateFolder(String username, Long folderId, DocumentFolderDto dto);

    void deleteFolder(String username, Long folderId);

    List<DocumentFolderDto> getFolders(String username);

    List<DocumentFolderDto> getFolderTree(String username);

    // ==================== Sharing ====================

    DocumentDto shareWithDoctor(String username, Long documentId, Long doctorId);

    DocumentDto unshareWithDoctor(String username, Long documentId, Long doctorId);

    DocumentDto toggleShareWithDoctor(String username, Long documentId);

    // ==================== Statistics ====================

    DocumentStats getStats(String username);

    // ==================== DTOs ====================

    record DocumentStats(
            long totalDocuments,
            long totalSize,
            String totalSizeFormatted,
            Map<DocumentType, Long> countByType,
            int folderCount
    ) {}
}
