package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.Gender;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PatientDataDto {

    private Long id;
    private Long patientId;
    private Integer age;
    private Gender gender;
    private String address;
    private Double weight;
    private Double height;
    private Long chatLogId;
    private List<String> symptoms;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
}
