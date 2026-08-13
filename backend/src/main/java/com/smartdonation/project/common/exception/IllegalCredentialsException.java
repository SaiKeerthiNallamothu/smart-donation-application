package com.smartdonation.project.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Thrown when the provided credentials are invalid (e.g. wrong password). */
public class IllegalCredentialsException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(IllegalCredentialsException.class);

    public IllegalCredentialsException(String message) {
        super(message);
        log.error("Illegal credentials -> {}", message);
    }
}
