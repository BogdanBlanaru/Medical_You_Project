package com.zega.medical_you_be.security;

import com.zega.medical_you_be.security.filters.JwtAuthenticationFilter;
import com.zega.medical_you_be.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/patient/profile/avatar/**").permitAll() // Public avatar access
                                .requestMatchers("/api/patient/profile/medical-id/**").permitAll() // Emergency access
                                .requestMatchers("/api/patient/profile/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                                .requestMatchers("/api/booking/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                                .requestMatchers("/api/user/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                                .requestMatchers("/api/chat-logs/**").hasAnyRole("ADMIN", "PATIENT")
                                .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                                .requestMatchers("/api/family/**").hasAnyRole("ADMIN", "PATIENT")
                                .requestMatchers("/api/health/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                                .requestMatchers("/api/medications/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                                .requestMatchers("/api/documents/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
