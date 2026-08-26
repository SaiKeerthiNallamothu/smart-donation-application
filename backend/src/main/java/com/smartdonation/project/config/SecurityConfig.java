package com.smartdonation.project.config;

import com.smartdonation.project.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Auth (public) ──────────────────────────────────────
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/verify-email",
                                "/api/v1/auth/login",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/refresh-token"
                        ).permitAll()

                        // ── Auth (authenticated) ────────────────────────────────
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/logout"
                        ).authenticated()

                        // ── Donor manages own profile ──────────────────────────
                        .requestMatchers(
                                "/api/v1/donor/profile/**"
                        ).hasRole("DONOR")

                        // ── NGO manages own profile ────────────────────────────
                        .requestMatchers(
                                "/api/v1/ngo/profile/**"
                        ).hasRole("NGO")

                        // ── Donor views verified NGOs ──────────────────────────
                        .requestMatchers(
                                "/api/v1/ngos/**"
                        ).hasRole("DONOR")

                        // ── Admin manages users ────────────────────────────────
                        .requestMatchers(
                                "/api/v1/users/**"
                        ).hasRole("ADMIN")

                        // ── Admin verifies NGOs ────────────────────────────────
                        .requestMatchers(
                                "/api/v1/admin/**"
                        ).hasRole("ADMIN")

                        // ── All remaining APIs require authentication ──────────
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
