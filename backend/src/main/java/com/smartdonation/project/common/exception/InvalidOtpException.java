package com.smartdonation.project.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Thrown when the provided OTP does not match the stored one. */
public class InvalidOtpException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(InvalidOtpException.class);

    public InvalidOtpException(String message) {
        super(message);
        log.error("Invalid OTP -> {}", message);
    }
}
