package com.zega.medical_you_be.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatLogDto {

    private Long id;
    private String prognosis;
    private String description;
    private String symptoms;
    private List<String> precautions;
    private List<String> complications;
    private Integer severity;
    private LocalDateTime createdAt;

}
