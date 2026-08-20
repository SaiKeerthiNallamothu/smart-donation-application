package com.smartdonation.project.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Thrown when a requested resource (e.g. profile) does not exist. */
public class ResourceNotFoundException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(ResourceNotFoundException.class);

    public ResourceNotFoundException(String message) {
        super(message);
        log.error("Resource not found -> {}", message);
    }
}
