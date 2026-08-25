package com.smartdonation.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingLicenseRequest {

    @NotBlank(message = "License number is required")
    @Size(max = 30, message = "License number must not exceed 30 characters")
    private String licenseNumber;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @NotBlank(message = "Issuing state is required")
    @Size(max = 100, message = "Issuing state must not exceed 100 characters")
    private String issuingState;

    @Size(max = 500, message = "Document URL must not exceed 500 characters")
    private String documentUrl;
}
