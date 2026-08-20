package com.smartdonation.project.common.exception;

import com.resend.core.exception.ResendException;
import org.slf4j.Logger;
import org.springframework.data.redis.serializer.SerializationException;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central exception handling so business exceptions return meaningful status
 * codes instead of raw 500s.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleIllegalCredentials(IllegalCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({InvalidOtpException.class, OtpExpiredException.class})
    public ResponseEntity<ErrorResponse> handleOtp(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);
        String detail = ex.getRootCause() != null ? ex.getRootCause().getMessage() : null;
        if (detail != null && detail.contains("Duplicate entry")) {
            return build(HttpStatus.CONFLICT, "A record with the same unique value already exists. Please check your input and try again.");
        }
        return build(HttpStatus.CONFLICT, "The requested operation conflicts with existing data. Please verify and try again.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String detail = cause != null ? cause.getMessage() : ex.getMessage();

        if (detail != null && detail.contains("Gender")) {
            return build(HttpStatus.BAD_REQUEST,
                    "Invalid value for 'gender'. Accepted values are: MALE, FEMALE, OTHER.");
        }
        if (detail != null && detail.contains("Role")) {
            return build(HttpStatus.BAD_REQUEST,
                    "Invalid value for 'role'. Accepted values are: DONOR, VOLUNTEER, ADMIN.");
        }
        if (detail != null && (detail.contains("enum") || detail.contains("Enum"))) {
            return build(HttpStatus.BAD_REQUEST,
                    "Invalid enum value provided. Please check the accepted values for this field.");
        }
        if (detail != null && detail.contains("date")) {
            return build(HttpStatus.BAD_REQUEST,
                    "Invalid date format. Please use ISO 8601 format (e.g. yyyy-MM-dd).", null);
        }
        // Fallback for other deserialization issues
        return build(HttpStatus.BAD_REQUEST,
                    "Malformed request body. Please verify the request payload and try again.");
    }

    @ExceptionHandler(ResendException.class)
    public ResponseEntity<ErrorResponse> handleResend(ResendException ex) {
        log.error("Resend email service error", ex);
        return build(HttpStatus.BAD_GATEWAY,
                "Unable to send verification email at this time. Please try again later.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST,
                String.format("Missing required request parameter '%s'.", ex.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "expected type";
        return build(HttpStatus.BAD_REQUEST,
                String.format("Invalid value '%s' for parameter '%s'. Expected type: %s.",
                        ex.getValue(), ex.getName(), expected));
    }

    @ExceptionHandler(SerializationException.class)
    public ResponseEntity<ErrorResponse> handleRedisSerialization(SerializationException ex) {
        log.error("Failed to deserialize data stored in Redis", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Session data could not be restored. Please try the operation again.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An internal server error occurred. If the issue persists, please contact support.");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message, null));
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message, errors));
    }
}
