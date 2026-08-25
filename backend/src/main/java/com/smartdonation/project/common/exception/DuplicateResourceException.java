package com.smartdonation.project.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Thrown when a resource already exists (e.g. duplicate profile). */
public class DuplicateResourceException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(DuplicateResourceException.class);

    public DuplicateResourceException(String message) {
        super(message);
        log.error("Duplicate resource -> {}", message);
    }
}
