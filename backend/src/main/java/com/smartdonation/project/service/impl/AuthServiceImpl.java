package com.smartdonation.project.service.impl;

import com.auth0.jwt.exceptions.JWTVerificationException;
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
import com.smartdonation.project.entities.User;
import com.smartdonation.project.enums.Role;
import com.smartdonation.project.repository.UserRepository;
import com.smartdonation.project.security.JwtUtil;
import com.smartdonation.project.service.AuthService;
import com.smartdonation.project.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String VERIFY_OTP_KEY_PREFIX = "verify-email:";
    private static final String PENDING_REGISTRATION_KEY_PREFIX = "register:";
    private static final String RESET_OTP_KEY_PREFIX = "reset-password:";
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";
    private static final Duration VERIFICATION_TTL = Duration.ofMinutes(5);
    private static final Duration RESET_TTL = Duration.ofMinutes(5);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    @Override
    public UserResponseDto registerUser(UserRegisterRequest userRegisterRequest)
            throws UserAlreadyExistException, ResendException {
        // Only one admin is allowed in the system. The first admin self-registers;
        // any later admin registration is rejected.
        if (userRegisterRequest.getRole() == Role.ADMIN && userRepository.existsByRole(Role.ADMIN)) {
            throw new IllegalArgumentException("An admin already exists. Only one admin account is allowed");
        }

        String email = userRegisterRequest.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistException("User already exists with email: " + email);
        }
        if (redisTemplate.hasKey(PENDING_REGISTRATION_KEY_PREFIX + email)) {
            throw new UserAlreadyExistException("Verification already pending for email: " + email);
        }

        // Do NOT save to the main DB yet - store the pending registration in Redis
        // with a verification OTP until the email is confirmed.
        String otp = generateOtp();
        redisTemplate.opsForValue().set(VERIFY_OTP_KEY_PREFIX + email, otp, VERIFICATION_TTL);
        redisTemplate.opsForValue().set(PENDING_REGISTRATION_KEY_PREFIX + email, userRegisterRequest, VERIFICATION_TTL);
        emailService.sendOtpEmail(email, otp);

        log.info("Verification OTP sent for email: {}", email);

        UserResponseDto response = new UserResponseDto();
        response.setEmail(email);
        response.setMessage("Verification code sent to your email");
        return response;
    }

    @Override
    @Transactional
    public UserResponseDto verifyEmail(VerifyOtpRequest verifyOtpRequest)
            throws InvalidOtpException, OtpExpiredException, UserAlreadyExistException {
        String email = verifyOtpRequest.getEmail().trim().toLowerCase();

        Object storedOtp = redisTemplate.opsForValue().get(VERIFY_OTP_KEY_PREFIX + email);
        Object storedRequest = redisTemplate.opsForValue().get(PENDING_REGISTRATION_KEY_PREFIX + email);

        if (storedOtp == null || storedRequest == null) {
            throw new OtpExpiredException("Verification code expired, please register again");
        }
        if (!storedOtp.toString().equals(verifyOtpRequest.getOtp())) {
            throw new InvalidOtpException("Invalid verification code");
        }

        UserRegisterRequest request = (UserRegisterRequest) storedRequest;

        // Safety net: even if two admin registrations were pending at the same time,
        // only one admin may ever be written to the database.
        if (request.getRole() == Role.ADMIN && userRepository.existsByRole(Role.ADMIN)) {
            throw new IllegalArgumentException("An admin already exists. Only one admin account is allowed");
        }

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .phone(request.getPhone().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .emailVerified(true)
                .build();

        User saved = userRepository.save(user);

        // Email verified - the pending data is no longer needed in Redis.
        redisTemplate.delete(VERIFY_OTP_KEY_PREFIX + email);
        redisTemplate.delete(PENDING_REGISTRATION_KEY_PREFIX + email);
        log.info("Email verified and user registered: {}", email);

        return UserResponseDto.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .role(saved.getRole())
                .message("Email verified, registration successful")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequest loginRequest) throws IllegalCredentialsException, UserNotFoundException {
        String email = loginRequest.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalCredentialsException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        log.info("User logged in with email: {}", email);

        return AuthResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .email(user.getEmail())
                .message("Login successful")
                .build();
    }

    @Override
    public AuthResponseDto refreshToken(RefreshTokenRequest refreshTokenRequest)
            throws IllegalCredentialsException {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        // Reject blacklisted refresh tokens
        if (Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + refreshToken))) {
            throw new IllegalCredentialsException("Refresh token has been revoked");
        }

        String email;
        try {
            email = jwtUtil.retrieveEmailFromToken(refreshToken);
        } catch (JWTVerificationException e) {
            throw new IllegalCredentialsException("Invalid or expired refresh token");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalCredentialsException("User not found for this refresh token"));

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        // Blacklist the old refresh token so it cannot be reused
        redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + refreshToken, "revoked", Duration.ofDays(1));

        log.info("Tokens refreshed for email: {}", email);

        return AuthResponseDto.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .message("Token refreshed successfully")
                .build();
    }

    @Override
    public void logout(String email) {
        // In a stateless JWT setup the server cannot truly invalidate a token,
        // but we log the event so the client knows to discard its tokens.
        log.info("User logged out: {}", email);
    }

    @Override
    public UserResponseDto forgotPassword(ForgotPasswordRequest forgotPasswordRequest)
            throws UserNotFoundException, ResendException {
        String email = forgotPasswordRequest.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Remove any previous reset OTP for this email
        redisTemplate.delete(RESET_OTP_KEY_PREFIX + email);

        String otp = generateOtp();
        redisTemplate.opsForValue().set(RESET_OTP_KEY_PREFIX + email, otp, RESET_TTL);
        emailService.sendOtpEmail(email, otp);

        log.info("Password reset OTP sent for email: {}", email);

        return UserResponseDto.builder()
                .email(email)
                .message("Password reset code sent to your email")
                .build();
    }

    @Override
    @Transactional
    public UserResponseDto resetPassword(ResetPasswordRequest resetPasswordRequest)
            throws InvalidOtpException, OtpExpiredException, UserNotFoundException {
        String email = resetPasswordRequest.getEmail().trim().toLowerCase();

        Object storedOtp = redisTemplate.opsForValue().get(RESET_OTP_KEY_PREFIX + email);

        if (storedOtp == null) {
            throw new OtpExpiredException("Password reset code expired, please request a new one");
        }
        if (!storedOtp.toString().equals(resetPasswordRequest.getOtp())) {
            throw new InvalidOtpException("Invalid reset code");
        }
        if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {
            throw new InvalidOtpException("Password and confirm password do not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(RESET_OTP_KEY_PREFIX + email);
        log.info("Password reset successful for email: {}", email);

        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .message("Password reset successful")
                .build();
    }

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
