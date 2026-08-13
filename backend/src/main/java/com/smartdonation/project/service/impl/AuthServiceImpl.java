package com.smartdonation.project.service.impl;

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
    private static final Duration VERIFICATION_TTL = Duration.ofMinutes(5);

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
        // Admin accounts are provisioned by an existing admin, never self-registered.
        if (userRegisterRequest.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be self-registered");
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
    public AuthResponseDto verifyEmail(VerifyOtpRequest verifyOtpRequest)
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
        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .phone(request.getPhone().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);

        // Email verified - the pending data is no longer needed in Redis.
        redisTemplate.delete(VERIFY_OTP_KEY_PREFIX + email);
        redisTemplate.delete(PENDING_REGISTRATION_KEY_PREFIX + email);
        log.info("Email verified and user registered: {}", email);

        String accessToken = jwtUtil.generateAccessToken(saved);
        String refreshToken = jwtUtil.generateRefreshToken(saved);

        return AuthResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .role(saved.getRole().name())
                .email(saved.getEmail())
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

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
