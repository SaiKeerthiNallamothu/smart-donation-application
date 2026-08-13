package com.smartdonation.project.dto.response;

import com.smartdonation.project.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after user registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    /** The newly created user id. */
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    /** Assigned role. */
    private Role role;

    /** Human-readable confirmation message. */
    private String message;
}
