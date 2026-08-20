package com.smartdonation.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO returned after a successful login (email/password or OAuth2). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    /** The JWT access token to send as {@code Authorization: Bearer <token>}. */
    private String token;

    /** The long-lived refresh token. */
    private String refreshToken;

    /** The authenticated user's role, e.g. {@code DONOR}, {@code NGO}, {@code ADMIN}. */
    private String firstName;

    private String lastName;

    private String role;

    private String email;

    private String message;
}
