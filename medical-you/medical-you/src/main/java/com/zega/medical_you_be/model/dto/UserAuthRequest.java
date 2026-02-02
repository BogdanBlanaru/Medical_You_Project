package com.zega.medical_you_be.model.dto;

import com.zega.medical_you_be.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthRequest {

    private String username;
    private String password;
    private Role role;
    private boolean rememberMe = false;
}
