package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.Role;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PatientDto {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;

}
