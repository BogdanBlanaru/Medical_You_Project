package com.zega.medical_you_be.model.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AdminDto {

    private Long id;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
}
