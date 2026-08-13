package com.smartdonation.project.service;

import com.resend.core.exception.ResendException;
import com.smartdonation.project.common.exception.IllegalCredentialsException;
import com.smartdonation.project.common.exception.InvalidOtpException;
import com.smartdonation.project.common.exception.OtpExpiredException;
import com.smartdonation.project.common.exception.UserAlreadyExistException;
import com.smartdonation.project.common.exception.UserNotFoundException;
import com.smartdonation.project.dto.request.LoginRequest;
import com.smartdonation.project.dto.request.UserRegisterRequest;
import com.smartdonation.project.dto.request.VerifyOtpRequest;
import com.smartdonation.project.dto.response.AuthResponseDto;
import com.smartdonation.project.dto.response.UserResponseDto;

public interface AuthService {

    /**
     * Registers a user tentatively: stores the pending registration in Redis and
     * sends a verification OTP by email. Nothing is written to the main database
     * until {@link #verifyEmail(VerifyOtpRequest)} succeeds.
     */
    UserResponseDto registerUser(UserRegisterRequest userRegisterRequest)
            throws UserAlreadyExistException, ResendException;

    /** Verifies the OTP and, on success, saves the user to the main database and returns tokens. */
    AuthResponseDto verifyEmail(VerifyOtpRequest verifyOtpRequest)
            throws InvalidOtpException, OtpExpiredException, UserAlreadyExistException;

    AuthResponseDto login(LoginRequest loginRequest) throws IllegalCredentialsException, UserNotFoundException;
}
