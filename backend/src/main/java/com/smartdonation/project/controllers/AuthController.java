package com.smartdonation.project.controllers;

import com.resend.core.exception.ResendException;
import com.smartdonation.project.common.exception.IllegalCredentialsException;
import com.smartdonation.project.common.exception.InvalidOtpException;
import com.smartdonation.project.common.exception.OtpExpiredException;
import com.smartdonation.project.common.exception.UserAlreadyExistException;
import com.smartdonation.project.common.exception.UserNotFoundException;
import com.smartdonation.project.dto.request.ForgotPasswordRequest;
import com.smartdonation.project.dto.request.LoginRequest;
import com.smartdonation.project.dto.request.RefreshTokenRequest;
import com.smartdonation.project.dto.request.ResetPasswordRequest;
import com.smartdonation.project.dto.request.UserRegisterRequest;
import com.smartdonation.project.dto.request.VerifyOtpRequest;
import com.smartdonation.project.dto.response.AuthResponseDto;
import com.smartdonation.project.dto.response.UserResponseDto;
import com.smartdonation.project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest)
            throws UserAlreadyExistException, ResendException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userRegisterRequest));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<UserResponseDto> verifyEmail(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest)
            throws InvalidOtpException, OtpExpiredException, UserAlreadyExistException {
        return ResponseEntity.ok(authService.verifyEmail(verifyOtpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequest loginRequest)
            throws IllegalCredentialsException, UserNotFoundException {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest)
            throws IllegalCredentialsException {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<UserResponseDto> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest)
            throws UserNotFoundException, ResendException {
        return ResponseEntity.ok(authService.forgotPassword(forgotPasswordRequest));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<UserResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest)
            throws InvalidOtpException, OtpExpiredException, UserNotFoundException {
        return ResponseEntity.ok(authService.resetPassword(resetPasswordRequest));
    }
}
