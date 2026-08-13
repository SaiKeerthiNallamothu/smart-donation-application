package com.smartdonation.project.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Thrown when a user tries to register with an email that is already in use. */
public class UserAlreadyExistException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(UserAlreadyExistException.class);

    public UserAlreadyExistException(String message) {
        super(message);
        log.error("User already exists -> {}", message);
    }
}
