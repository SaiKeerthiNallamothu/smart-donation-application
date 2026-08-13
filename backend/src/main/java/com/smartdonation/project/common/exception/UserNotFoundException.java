package com.smartdonation.project.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Thrown when a user cannot be found (e.g. during login). */
public class UserNotFoundException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(UserNotFoundException.class);

    public UserNotFoundException(String message) {
        super(message);
        log.error("User not found -> {}", message);
    }
}
