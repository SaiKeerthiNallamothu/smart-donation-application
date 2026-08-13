package com.smartdonation.project.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Thrown when the OTP (or the pending registration) has expired. */
public class OtpExpiredException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(OtpExpiredException.class);

    public OtpExpiredException(String message) {
        super(message);
        log.error("OTP expired -> {}", message);
    }
}
