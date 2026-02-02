package com.zega.medical_you_be.service;

import com.zega.medical_you_be.model.entity.Doctor;
import com.zega.medical_you_be.model.entity.Patient;
import com.zega.medical_you_be.model.enums.Role;
import com.zega.medical_you_be.repo.DoctorRepo;
import com.zega.medical_you_be.repo.PatientRepo;
import com.zega.medical_you_be.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;
    @Setter
    private Role role;

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // If role is specified, search in the specific table
        if (role != null && role.equals(Role.DOCTOR)) {
            Optional<Doctor> doctor = doctorRepo.findByEmail(username);
            if (doctor.isPresent()) {
                var user = doctor.get();
                LOGGER.info(String.valueOf(user));
                return User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(String.valueOf(user.getRole()))
                        .build();
            }
        } else if (role != null && role.equals(Role.PATIENT)) {
            Optional<Patient> patient = patientRepo.findByEmail(username);
            if (patient.isPresent()) {
                var user = patient.get();
                LOGGER.info(String.valueOf(user));
                return User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(String.valueOf(user.getRole()))
                        .build();
            }
        } else {
            // Role not specified (e.g., from JWT filter) - search both tables
            // Try patient first (more common)
            Optional<Patient> patient = patientRepo.findByEmail(username);
            if (patient.isPresent()) {
                var user = patient.get();
                LOGGER.info(String.valueOf(user));
                return User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(String.valueOf(user.getRole()))
                        .build();
            }

            // Try doctor
            Optional<Doctor> doctor = doctorRepo.findByEmail(username);
            if (doctor.isPresent()) {
                var user = doctor.get();
                LOGGER.info(String.valueOf(user));
                return User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(String.valueOf(user.getRole()))
                        .build();
            }
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
