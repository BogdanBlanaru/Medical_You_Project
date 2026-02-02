package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.DocumentType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long id;
    private Long patientId;
    private Long familyMemberId;
    private String familyMemberName;
    private Long folderId;
    private String folderName;
    private DocumentType documentType;
    private String title;
    private String description;
    private String fileName;
    private Long fileSize;
    private String fileSizeFormatted;
    private String mimeType;
    private String thumbnailUrl;
    private List<String> tags;
    private Boolean isSharedWithDoctor;
    private List<Long> sharedWithDoctors;
    private LocalDate documentDate;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

    // Helper fields
    private boolean isImage;
    private boolean isPdf;
    private String fileExtension;
}
