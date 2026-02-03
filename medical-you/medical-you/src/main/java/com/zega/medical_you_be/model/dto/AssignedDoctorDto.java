package com.zega.medical_you_be.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignedDoctorDto {
    private Long id;
    private String name;
    private String email;
    private String specialization;
    private String hospital;
    private Double rating;
}
