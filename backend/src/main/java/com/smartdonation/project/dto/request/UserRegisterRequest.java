package com.smartdonation.project.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartdonation.project.enums.Role;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  UserRegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+91[\\-\\s]?)?[6-9]\\d{9}$",
            message = "Phone number must be a valid Indian mobile number (e.g. +91 98765 43210 or 9876543210)")
    private String phone;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#$%^&+=!])[A-Za-z0-9@#$%^&+=!]{8,64}$",
            message = "Password must be 8-64 characters with at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character (@ # $ % ^ & + = !)")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotNull(message = "Role is required")
    private Role role;

    @JsonIgnore
    @AssertTrue(message = "Password and confirm password must match")
    public boolean isPasswordConfirmed() {
        return password == null || password.equals(confirmPassword);
    }
}
