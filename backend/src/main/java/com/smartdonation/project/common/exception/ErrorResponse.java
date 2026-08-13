package com.smartdonation.project.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Uniform error body returned by {@link GlobalExceptionHandler}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;

    private String message;

    /** Per-field validation errors (only present for 400 validation responses). */
    private Map<String, String> errors;
}
