package com.zega.medical_you_be.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFolderDto {
    private Long id;
    private Long patientId;

    @NotBlank(message = "Folder name is required")
    private String name;

    private Long parentFolderId;
    private String parentFolderName;
    private String icon;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested data
    private List<DocumentFolderDto> subFolders;
    private int documentCount;
}
