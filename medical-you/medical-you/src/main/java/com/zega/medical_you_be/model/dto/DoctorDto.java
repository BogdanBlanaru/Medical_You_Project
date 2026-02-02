package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.Role;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DoctorDto {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String specialization;
    private String hospital;
    private String hospitalAddress;
    private Double rating;
    private Integer yearsOfExperience;
    private String education;
    private String officeHours;
    private String contactNumber;
    private Role role;
}
