package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentDto {

    private Long familyMemberId;

    private Long folderId;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private List<String> tags;

    private LocalDate documentDate;

    private Boolean shareWithDoctor;
}
